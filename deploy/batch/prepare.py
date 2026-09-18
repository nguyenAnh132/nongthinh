#!/usr/bin/env python3
"""Run interactively as root on the deploy VPS; no secrets leave this machine."""
import getpass
import json
import os
from pathlib import Path
import secrets
import subprocess
import sys

ROOT = Path('/opt/nongthinh')
HERE = Path(__file__).resolve().parent
SERVICES = json.loads((HERE / 'services.json').read_text())


def ask_secret(label):
    while True:
        value = getpass.getpass(label + ': ')
        again = getpass.getpass('Nhap lai: ')
        if value and value == again and '\n' not in value and '\r' not in value:
            return value
        print('Gia tri rong hoac khong khop; vui long nhap lai.')


def required(mapping, name):
    value = mapping.get(name)
    if not value or str(value).startswith('THAY_'):
        raise SystemExit('Thieu cau hinh trong auth/profile hien tai: ' + name)
    return str(value)


def write_env(path, values):
    # Compose env_file format: raw preserves $, quotes and backslashes literally.
    for value in values.values():
        if '\n' in str(value) or '\r' in str(value):
            raise SystemExit('Khong ho tro gia tri env nhieu dong.')
    with path.open('x', encoding='utf-8', newline='\n') as stream:
        for name, value in values.items():
            stream.write(f'{name}={value}\n')
    path.chmod(0o600)


def main():
    if os.geteuid() != 0:
        raise SystemExit('Chay bang root tren VPS deploy.')
    os.umask(0o077)
    os.chdir(ROOT)
    output = ROOT / 'env/batch'
    if output.exists():
        raise SystemExit('env/batch da ton tai. Khong ghi de secret; sua file can thiet bang nano.')
    base = ['docker', 'compose', '-f', 'compose.prod.yml']
    result = subprocess.run(base + ['config', '--format', 'json'], capture_output=True, text=True)
    if result.returncode:
        raise SystemExit('Compose goc khong hop le. Chay docker compose -f compose.prod.yml config --quiet')
    # Parse the effective environment in memory only; never print the config.
    config = json.loads(result.stdout)
    auth = config['services']['auth-service']['environment']
    profile = config['services']['profile-service']['environment']
    issuer = required(auth, 'KEYCLOAK_ISSUER_URI')
    kafka = required(auth, 'KAFKA_BOOTSTRAP_SERVERS')
    profile_key = required(profile, 'PROFILE_SERVICE_API_KEY')
    if profile_key != required(auth, 'PROFILE_SERVICE_API_KEY'):
        raise SystemExit('PROFILE_SERVICE_API_KEY giua auth.env va profile.env khong khop.')
    location_key = required(profile, 'LOCATION_SERVICE_API_KEY')
    redis = {k: required(auth, k) for k in ('REDIS_HOST', 'REDIS_PORT', 'REDIS_PASSWORD')}
    keys = {s['prefix'] + '_API_KEY': secrets.token_hex(32) for s in SERVICES}
    keys['LOCATION_SERVICE_API_KEY'] = location_key
    keys['PROFILE_SERVICE_API_KEY'] = profile_key
    envs = {}
    for svc in SERVICES:
        while True:
            password = ask_secret('Mat khau PostgreSQL da dat cho ' + svc['user'])
            check = subprocess.run(base + [
                'exec', '-T', '-e', 'PGPASSWORD', 'postgres', 'psql',
                '-h', 'postgres', '-U', svc['user'], '-d', svc['db'],
                '-v', 'ON_ERROR_STOP=1', '-Atc', 'SELECT 1'
            ], env={**os.environ, 'PGPASSWORD': password}, capture_output=True, text=True)
            if check.returncode == 0 and check.stdout.strip() == '1':
                break
            print('Dang nhap database chua thanh cong. Kiem tra mat khau va database; Ctrl+C de dung.')
        prefix = svc['prefix']
        envs[svc['name']] = {
            'SPRING_PROFILES_ACTIVE': 'prod',
            'SERVER_PORT': str(svc['port']),
            'SERVER_SERVLET_CONTEXT_PATH': svc['context'],
            prefix + '_DB_URL': f"jdbc:postgresql://postgres:5432/{svc['db']}",
            prefix + '_DB_USERNAME': svc['user'],
            prefix + '_DB_PASSWORD': password,
            prefix + '_API_KEY': keys[prefix + '_API_KEY'],
            'KEYCLOAK_ISSUER_URI': issuer,
            'SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE': '5',
            'SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE': '1',
            'LOGGING_LEVEL_COM_NONGTHINH': 'INFO',
            'JAVA_TOOL_OPTIONS': '-XX:InitialRAMPercentage=10.0 -XX:MaxRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError',
            'BRAND_ACCESS_PROFILE_SERVICE_URL': 'http://profile-service:9092/profile',
        }
    envs['notification-service'].update({
        'KAFKA_BOOTSTRAP_SERVERS': kafka,
        'MAIL_HOST': 'smtp.gmail.com', 'MAIL_PORT': '587',
        'MAIL_USERNAME': input('Dia chi Gmail gui thong bao: ').strip(),
        'MAIL_PASSWORD': ask_secret('Gmail App Password (khong phai mat khau dang nhap)'),
    })
    if not envs['notification-service']['MAIL_USERNAME']:
        raise SystemExit('MAIL_USERNAME khong duoc rong.')
    envs['brand-service'].update({
        'KAFKA_BOOTSTRAP_SERVERS': kafka,
        'BRAND_PROFILE_SERVICE_URL': 'http://profile-service:9092/profile',
        'PROFILE_SERVICE_API_KEY': profile_key,
        'BRAND_CAMUNDA_ADMIN_USER_ID': 'admin',
        'BRAND_CAMUNDA_ADMIN_USER_PASSWORD': ask_secret('Mat khau moi cho Camunda admin'),
        # New DB: Flyway creates application tables, Camunda creates ACT_* tables.
        'BRAND_CAMUNDA_DATABASE_SCHEMA_UPDATE': 'true',
    })
    envs['file-service'].update(redis)
    envs['file-service'].update({
        'FILE_BO_PORTAL_SERVICE_URL': 'http://bo-portal-service:9096/bo-portal',
        'BO_PORTAL_SERVICE_API_KEY': keys['BO_PORTAL_SERVICE_API_KEY'],
        'FILE_PUBLIC_BASE_URL': 'https://nongthinh.nvtanh.id.vn/api/v1/files/public',
        'FILE_STORAGE_BASE_PATH': '/data/storage',
        'FILE_STORAGE_BUCKET': 'nongthinh-files',
    })
    envs['agri-catalog-service'].update({
        'AGRI_CATALOG_FILE_SERVICE_URL': 'http://file-service:9095/files',
        'FILE_SERVICE_API_KEY': keys['FILE_SERVICE_API_KEY'],
        'AGRI_CATALOG_DIAGNOSIS_SERVICE_URL': 'http://rice-disease-diagnosis-service:9099/diagnosis',
        'RICE_DISEASE_DIAGNOSIS_SERVICE_API_KEY': keys['RICE_DISEASE_DIAGNOSIS_SERVICE_API_KEY'],
    })
    envs['post-service'].update({
        'KAFKA_BOOTSTRAP_SERVERS': kafka,
        'POST_PROFILE_SERVICE_URL': 'http://profile-service:9092/profile',
        'POST_FILE_SERVICE_URL': 'http://file-service:9095/files',
        'FILE_SERVICE_API_KEY': keys['FILE_SERVICE_API_KEY'],
        'POST_AGRI_CATALOG_SERVICE_URL': 'http://agri-catalog-service:9098/agri-catalog',
        'POST_SERVICE_DB_POOL_MAX_SIZE': '5', 'POST_SERVICE_DB_POOL_MIN_IDLE': '1',
    })
    envs['rice-disease-diagnosis-service'].update({
        'RICE_DISEASE_DIAGNOSIS_AGRI_CATALOG_URL': 'http://agri-catalog-service:9098/agri-catalog',
        'AGRI_CATALOG_SERVICE_API_KEY': keys['AGRI_CATALOG_SERVICE_API_KEY'],
        'RICE_DISEASE_DIAGNOSIS_FILE_SERVICE_URL': 'http://file-service:9095/files',
        'FILE_SERVICE_API_KEY': keys['FILE_SERVICE_API_KEY'],
        'DIAGNOSIS_MAX_CONCURRENT_INFERENCES': '1',
        'MODEL_VALIDATION_MAX_CACHED_MODEL_VERSIONS': '1',
    })
    gateway = {
        'SPRING_PROFILES_ACTIVE': 'prod', 'API_GATEWAY_PORT': '8888',
        'CORS_ALLOWED_ORIGINS': 'https://nongthinh.nvtanh.id.vn',
        'JAVA_TOOL_OPTIONS': '-XX:InitialRAMPercentage=10.0 -XX:MaxRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError',
        'AUTH_SERVICE_URL': 'http://auth-service:9090',
        'PROFILE_SERVICE_URL': 'http://profile-service:9092',
    }
    # Gateway retains service path after StripPrefix=2; its upstream URLs have no context path.
    for svc in SERVICES:
        key = svc['prefix'] + '_URL'
        if svc['name'] == 'bo-portal-service':
            key = 'API_GATEWAY_BO_PORTAL_SERVICE_URL'
        gateway[key] = f"http://{svc['name']}:{svc['port']}"
    envs['api-gateway'] = gateway
    storage = ROOT / 'data/files'
    if not storage.exists():
        storage.mkdir(parents=True, mode=0o750)
        os.chown(storage, 10001, 10001)
    elif storage.stat().st_uid != 10001:
        raise SystemExit('data/files da ton tai nhung owner khong phai UID 10001; kiem tra quyen truoc.')
    output.mkdir(mode=0o700)
    for name, values in envs.items():
        write_env(output / (name + '.env'), values)
    print('BATCH_ENV_READY: 9 files, DB passwords verified, shared API keys synchronized.')
    print('env/location.env cu khong duoc dung trong batch nay; cau hinh moi o env/batch/.')


if __name__ == '__main__':
    try:
        main()
    except KeyboardInterrupt:
        sys.exit('\nDa dung. Khong thay doi database.')
