"""Offline tests for CI planning/state only; no application tests or Docker needed."""
import json
import os
from pathlib import Path
import subprocess
import tempfile
import unittest
from unittest.mock import patch

import release

OLD = 'a' * 40
NEW = 'b' * 40
TAG = 'batch-8-bbbbbbbbbbbb'


def state():
    return dict(commit=OLD, dirty=False,
                tags={s: 'batch-7-aaaaaaaaaaaa' for s in release.BACKENDS})


class SelectionTests(unittest.TestCase):
    def test_one_backend(self):
        paths = ['services/profile-service/src/Main.java']
        self.assertEqual(release.select('backend', paths), ['profile-service'])
        self.assertEqual(release.select('frontend', paths), [])

    def test_frontend_only(self):
        paths = ['apps/web/nongthinh/src/app/app.ts']
        self.assertEqual(release.select('backend', paths), [])
        self.assertEqual(release.select('frontend', paths), ['frontend'])

    def test_docs_only(self):
        paths = ['docs/a.md', 'README.md', 'deploy/batch/README.md']
        self.assertEqual(release.select('backend', paths), [])
        self.assertEqual(release.select('frontend', paths), [])

    def test_shared_and_unknown(self):
        for path in ['ci/release.py', 'pom.xml', 'services/shared/pom.xml', '.dockerignore']:
            self.assertEqual(release.select('backend', [path]), release.BACKENDS)
            self.assertEqual(release.select('frontend', [path]), ['frontend'])

    def test_job_config_is_scoped(self):
        self.assertEqual(release.select('frontend', ['deploy/batch/deploy.sh']), [])
        self.assertEqual(release.select('backend', ['ci/Jenkinsfile.frontend']), [])
        self.assertEqual(release.select('backend', ['deploy/batch/deploy.sh']), release.BACKENDS)

    def test_move_between_services(self):
        self.assertEqual(release.select('backend', ['services/auth-service/A.java',
                                                   'services/profile-service/A.java']),
                         ['profile-service', 'auth-service'])


class PlanTests(unittest.TestCase):
    def make_plan(self, previous, paths=b'', force=False):
        with patch.object(release, 'git', return_value=NEW), \
             patch.object(release.subprocess, 'run'), \
             patch.object(release.subprocess, 'check_output', return_value=paths):
            return release.plan('backend', previous, TAG, force)

    def test_initial_full(self):
        self.assertEqual(self.make_plan({})['selected'], release.BACKENDS)

    def test_unchanged_versions_preserved(self):
        result = self.make_plan(state(), b'services/profile-service/pom.xml\0')
        self.assertEqual(result['selected'], ['profile-service'])
        self.assertEqual(result['tags']['profile-service'], TAG)
        self.assertEqual(result['tags']['auth-service'], state()['tags']['auth-service'])

    def test_noop(self):
        self.assertEqual(self.make_plan(state())['selected'], [])

    def test_force_and_dirty(self):
        self.assertEqual(self.make_plan(state(), force=True)['selected'], release.BACKENDS)
        self.assertEqual(self.make_plan(dict(state(), dirty=True))['selected'], release.BACKENDS)

    def test_missing_tag(self):
        previous = state()
        del previous['tags']['profile-service']
        self.assertEqual(self.make_plan(previous)['selected'], release.BACKENDS)

    def test_missing_or_rewritten_history(self):
        with patch.object(release, 'git', return_value=NEW), \
             patch.object(release.subprocess, 'run', side_effect=subprocess.CalledProcessError(1, 'git')):
            self.assertEqual(release.plan('backend', state(), TAG)['selected'], release.BACKENDS)

    def test_frontend(self):
        previous = dict(commit=OLD, dirty=False, tags={'frontend': 'web-1-aaaaaaaaaaaa'})
        with patch.object(release, 'git', return_value=NEW), \
             patch.object(release.subprocess, 'run'), \
             patch.object(release.subprocess, 'check_output', return_value=b'apps/web/nongthinh/a.ts\0'):
            self.assertEqual(release.plan('frontend', previous, 'web-2-bbbbbbbbbbbb')['selected'], ['frontend'])


class StateTests(unittest.TestCase):
    def test_only_success_advances_baseline(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'backend.json'
            previous = state()
            release.write_json(path, previous)
            plan = dict(baseline=previous, commit=NEW, tags=previous['tags'])
            release.transition('begin', plan, path)
            pending = release.read_state(path)
            self.assertTrue(pending['dirty'])
            self.assertEqual(pending['commit'], OLD)
            release.transition('finish', plan, path)
            self.assertEqual(release.read_state(path)['commit'], NEW)
            self.assertFalse(release.read_state(path)['dirty'])

    def test_stale_release_refused(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'backend.json'
            release.write_json(path, state())
            with self.assertRaises(RuntimeError):
                release.transition('begin', dict(baseline={}, commit=NEW), path)
            self.assertEqual(release.read_state(path), state())


class GitHistoryTests(unittest.TestCase):
    def test_diff_spans_all_undeployed_commits_and_rename(self):
        original = Path.cwd()
        with tempfile.TemporaryDirectory() as directory:
            try:
                os.chdir(directory)
                release.git('init', '-q')
                release.git('config', 'user.name', 'CI Test')
                release.git('config', 'user.email', 'ci@example.invalid')
                path = Path('services/auth-service/source.txt')
                path.parent.mkdir(parents=True)
                path.write_text('initial')
                release.git('add', '.')
                release.git('commit', '-qm', 'initial')
                previous = dict(state(), commit=release.git('rev-parse', 'HEAD'))
                # Build-only or failed build must not advance the deployment baseline.
                path.write_text('changed')
                release.git('commit', '-qam', 'auth changed')
                profile = Path('services/profile-service/source.txt')
                profile.parent.mkdir(parents=True)
                path.rename(profile)
                release.git('add', '-A')
                release.git('commit', '-qm', 'move between services')
                result = release.plan('backend', previous, TAG)
                self.assertEqual(result['selected'], ['profile-service', 'auth-service'])
                # The plan includes immutable tags for every service, not just selected ones.
                self.assertEqual(len(result['tags']), 11)
            finally:
                os.chdir(original)


if __name__ == '__main__':
    unittest.main()
