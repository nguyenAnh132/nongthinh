#!/usr/bin/env python3
"""Selective releases; state lives on the deploy host, never in Jenkins history."""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess

BACKENDS = ['location-service', 'bo-portal-service', 'notification-service',
            'brand-service', 'file-service', 'agri-catalog-service', 'post-service',
            'rice-disease-diagnosis-service', 'profile-service', 'auth-service', 'api-gateway']


def git(*args):
    return subprocess.check_output(['git', *args]).decode().strip()


def read_state(path):
    return json.loads(path.read_text()) if path.exists() else {}


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    temp = path.with_suffix('.tmp')
    temp.write_text(json.dumps(data, indent=2) + '\n')
    os.replace(temp, path)


def select(kind, paths):
    selected = set()
    services = BACKENDS if kind == 'backend' else ['frontend']
    for path in paths:
        # Only clearly non-runtime documentation is ignored globally.
        if path.startswith('docs/') or Path(path).name in ('README.md', 'DESIGN.md'):
            continue
        if path.startswith('apps/web/nongthinh/'):
            if kind == 'frontend':
                selected.add('frontend')
        elif path.startswith('services/') and path.split('/')[1] in BACKENDS:
            if kind == 'backend':
                selected.add(path.split('/')[1])
        elif path.startswith(('ci/Jenkinsfile.frontend', 'ci/build-frontend.sh', 'deploy/frontend/')):
            if kind == 'frontend':
                selected.update(services)
        elif path.startswith(('ci/Jenkinsfile.remaining', 'ci/build-remaining.sh', 'deploy/batch/')):
            if kind == 'backend':
                selected.update(services)
        else:
            # Unknown/shared inputs conservatively invalidate all services in this job.
            selected.update(services)
    return [service for service in services if service in selected]


def plan(kind, state, tag, force=False):
    services = BACKENDS if kind == 'backend' else ['frontend']
    prefix = 'batch' if kind == 'backend' else 'web'
    if not re.fullmatch(prefix + r'-\d+-[a-f0-9]{12}', tag):
        raise ValueError('Invalid image tag')
    commit = git('rev-parse', 'HEAD')
    baseline = state.get('commit', '')
    tags = state.get('tags', {})
    complete = all(re.fullmatch(prefix + r'-\d+-[a-f0-9]{12}', tags.get(s, '')) for s in services)
    reason = 'changed paths'
    if force or state.get('dirty') or not complete or not re.fullmatch(r'[a-f0-9]{40}', baseline):
        chosen = services[:]
        reason = 'forced, initial, incomplete or previously interrupted deployment'
    else:
        try:
            subprocess.run(['git', 'merge-base', '--is-ancestor', baseline, commit], check=True,
                           stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
            # --no-renames includes both deleted and added paths for moves between services.
            paths = subprocess.check_output(['git', 'diff', '--name-only', '--no-renames',
                                             '-z', baseline, commit]).decode().split('\0')
            chosen = select(kind, [p for p in paths if p])
        except subprocess.CalledProcessError:
            chosen = services[:]
            reason = 'baseline unavailable or history rewritten'
    next_tags = {s: tag if s in chosen else tags[s] for s in services}
    return dict(kind=kind, commit=commit, baseline=state, selected=chosen,
                tags=next_tags, reason=reason)


def transition(action, release, path):
    state = read_state(path)
    pending = dict(release['baseline'], dirty=True, pending=release['commit'])
    expected = release['baseline'] if action == 'begin' else pending
    if state != expected:
        raise RuntimeError('Deployment state changed since planning; rebuild from current state')
    if action == 'begin':
        write_json(path, pending)
    else:
        write_json(path, dict(commit=release['commit'], tags=release['tags'], dirty=False))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=['plan', 'begin', 'finish'])
    parser.add_argument('--kind', choices=['backend', 'frontend'], required=True)
    parser.add_argument('--state', type=Path, required=True)
    parser.add_argument('--plan', type=Path, default=Path('release-plan.json'))
    parser.add_argument('--tag')
    parser.add_argument('--force', action='store_true')
    args = parser.parse_args()
    if args.action == 'plan':
        release = plan(args.kind, read_state(args.state), args.tag, args.force)
        write_json(args.plan, release)
        Path('selected-services.txt').write_text(''.join(s + '\n' for s in release['selected']))
        # Each backend has its own image variable, preserving untouched versions.
        image_env = ''.join(
            s.upper().replace('-', '_') + '_TAG=' + tag + '\n'
            for s, tag in release['tags'].items())
        if args.kind == 'backend':
            # Compatibility with existing base Compose interpolation on the VPS.
            for alias, service in [('AUTH', 'auth-service'), ('PROFILE', 'profile-service'),
                                   ('GATEWAY', 'api-gateway'), ('REMAINING', 'location-service')]:
                image_env += alias + '_TAG=' + release['tags'][service] + '\n'
        Path('release-images.env').write_text(image_env)
        print('SELECTED: ' + (', '.join(release['selected']) or 'none (skip build/deploy)'))
        print('REASON: ' + release['reason'])
    else:
        release = json.loads(args.plan.read_text())
        if release['kind'] != args.kind:
            raise ValueError('Release kind mismatch')
        transition(args.action, release, args.state)


if __name__ == '__main__':
    main()
