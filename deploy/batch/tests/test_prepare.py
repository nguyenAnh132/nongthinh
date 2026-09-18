import importlib.util
import json
from pathlib import Path
import re
import subprocess
import tempfile
import unittest
from unittest.mock import patch

MODULE = Path(__file__).resolve().parents[1] / 'prepare.py'
spec = importlib.util.spec_from_file_location('prepare_batch', MODULE)
prepare = importlib.util.module_from_spec(spec)
spec.loader.exec_module(prepare)
REPO = MODULE.parents[2]


class PrepareTests(unittest.TestCase):
    def test_generated_envs_cover_required_config_and_keep_shared_keys(self):
        base = {'services': {
            'auth-service': {'environment': {
                'KEYCLOAK_ISSUER_URI': 'https://auth.example/realms/test',
                'KAFKA_BOOTSTRAP_SERVERS': 'broker:9002',
                'PROFILE_SERVICE_API_KEY': 'existing-profile-key',
                'REDIS_HOST': 'redis', 'REDIS_PORT': '6379',
                'REDIS_PASSWORD': 'redis-$-password',
            }},
            'profile-service': {'environment': {
                'PROFILE_SERVICE_API_KEY': 'existing-profile-key',
                'LOCATION_SERVICE_API_KEY': 'existing-location-key',
            }},
        }}
        db_checks = []

        def run(command, **kwargs):
            if 'config' in command:
                return subprocess.CompletedProcess(command, 0, json.dumps(base), '')
            self.assertEqual(kwargs['env']['PGPASSWORD'], 'test-$-password')
            self.assertNotIn('test-$-password', command)
            db_checks.append(command[command.index('-U') + 1])
            return subprocess.CompletedProcess(command, 0, '1\n', '')

        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / 'env').mkdir()
            with patch.object(prepare, 'ROOT', root), \
                 patch.object(prepare.os, 'geteuid', return_value=0, create=True), \
                 patch.object(prepare.os, 'chown', create=True), \
                 patch.object(prepare.os, 'chdir'), \
                 patch.object(prepare.os, 'umask'), \
                 patch.object(prepare.subprocess, 'run', side_effect=run), \
                 patch.object(prepare.getpass, 'getpass', return_value='test-$-password'), \
                 patch('builtins.input', return_value='test@example.com'), \
                 patch('builtins.print'):
                prepare.main()
            values = {}
            for file in (root / 'env/batch').glob('*.env'):
                values[file.stem] = dict(line.split('=', 1) for line in file.read_text().splitlines())
            self.assertEqual(len(values), 9)
            self.assertEqual(len(db_checks), 8)
            self.assertEqual(values['location-service']['LOCATION_SERVICE_API_KEY'], 'existing-location-key')
            self.assertEqual(values['brand-service']['PROFILE_SERVICE_API_KEY'], 'existing-profile-key')
            self.assertEqual(values['file-service']['REDIS_PASSWORD'], 'redis-$-password')
            for service, env in values.items():
                for resource in (REPO / 'services' / service / 'src/main/resources').glob('application-prod.*'):
                    required = re.findall(r'\$\{([A-Z][A-Z0-9_]*)\}', resource.read_text(encoding='utf-8'))
                    self.assertFalse(set(required) - set(env), (service, set(required) - set(env)))
            for key in ('FILE_SERVICE_API_KEY', 'AGRI_CATALOG_SERVICE_API_KEY',
                        'RICE_DISEASE_DIAGNOSIS_SERVICE_API_KEY', 'BO_PORTAL_SERVICE_API_KEY'):
                self.assertEqual(len({env[key] for env in values.values() if key in env}), 1, key)
            self.assertEqual(values['api-gateway']['PROFILE_SERVICE_URL'], 'http://profile-service:9092')
            self.assertEqual(values['brand-service']['BRAND_PROFILE_SERVICE_URL'], 'http://profile-service:9092/profile')

    def test_refuses_to_overwrite_existing_secret_directory(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / 'env/batch').mkdir(parents=True)
            with patch.object(prepare, 'ROOT', root), \
                 patch.object(prepare.os, 'geteuid', return_value=0, create=True), \
                 patch.object(prepare.os, 'chdir'), \
                 patch.object(prepare.os, 'umask'), \
                 patch.object(prepare.subprocess, 'run') as run:
                with self.assertRaisesRegex(SystemExit, 'da ton tai'):
                    prepare.main()
                run.assert_not_called()


if __name__ == '__main__':
    unittest.main()
