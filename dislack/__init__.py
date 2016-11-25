# encoding: utf-8
from __future__ import unicode_literals, print_function

import argparse
import logging
import time
import os.path
import subprocess

logger = logging.getLogger('dislack')

RESOURCES_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), 'resources'))


def parse_args(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--sleep', help='sleep time in minutes', type=int, default=15)
    parser.set_defaults(
        open_path='/usr/bin/open',
        slack_path='/Applications/Slack.app',
        slack_icon=os.path.join(RESOURCES_DIR, 'slack_icon.png'),
        terminal_notifier_path='/usr/local/bin/terminal-notifier'
    )
    return parser.parse_args(argv)


def open_slack(app_options):
    try:
        subprocess.check_call([app_options.open_path, app_options.slack_path])
    except subprocess.CalledProcessError:
        return False
    return True


def send_notification(app_options):
    try:
        subprocess.check_call([
            app_options.terminal_notifier_path,
            '-appIcon', app_options.slack_icon,
            '-title', 'diSlack',
            '-message', 'Time to read Slack!',
        ])
    except subprocess.CalledProcessError:
        return False
    return True


def main(argv):
    app_options = parse_args(argv)
    while True:
        minute = 0
        logger.info('sleeping for {} minutes'.format(app_options.sleep))
        skipped = False
        try:
            while minute < app_options.sleep:
                time.sleep(60)
                minute += 1
                logger.debug('.')
        except KeyboardInterrupt:
            logger.info('skipping')
            skipped = True
        if not skipped:
            logger.info('time to read Slack')
            open_slack(app_options)
            send_notification(app_options)
        else:
            logger.info('Now you have 3 seconds to press Ctrl+C for exiting diSlack or else I\'ll launch Slack for you')
            try:
                time.sleep(3)
            except KeyboardInterrupt:
                break
            open_slack(app_options)
    logger.info('buy')
    return True

