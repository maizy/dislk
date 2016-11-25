# encoding: utf-8
from __future__ import unicode_literals, print_function

import argparse
import logging
import time
import os.path
import subprocess

logger = logging.getLogger('dislack')

RESOURCES_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), 'resources'))
MESSAGE = 'Time to read Slack!'


def parse_args(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument('-s', '--sleep', help='sleep time in minutes', type=int, default=15)
    parser.set_defaults(
        open_path='/usr/bin/open',
        pgrep_path='/usr/bin/pgrep',
        slack_path='/Applications/Slack.app',
        slack_icon=os.path.join(RESOURCES_DIR, 'slack_icon.png'),
        terminal_notifier_path='/usr/local/bin/terminal-notifier',
        slack_wait_timeout=15,
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
            '-message', MESSAGE,
        ])
    except subprocess.CalledProcessError:
        return False
    return True


def is_slack_open(app_options):
    try:
        output = subprocess.check_output([
            app_options.pgrep_path,
            '-f',
            '{}/Contents/MacOS/Slack'.format(app_options.slack_path),
        ])
        output = output.strip('\n ')
        if output.isdigit():
            return True
        return False
    except subprocess.CalledProcessError:
        return False


def wait_for_slack_closed(app_options):
    while is_slack_open(app_options):
        try:
            time.sleep(app_options.slack_wait_timeout)
        except KeyboardInterrupt:
            print()
            break
    return True


def wait_and_notice(app_options):
    logger.info('You will notice in {} minutes.'.format(app_options.sleep))
    logger.info('Press Ctrl+C if you want to skip waiting and open Slack.')
    minute = 0
    skipped = False
    try:
        while minute < app_options.sleep:
            time.sleep(60)
            minute += 1
            logger.debug('.')
    except KeyboardInterrupt:
        print()
        logger.info('Waiting skipped')
        skipped = True
    if not skipped:
        logger.info(MESSAGE)
        open_slack(app_options)
        send_notification(app_options)
    else:
        logger.info('Now you have 3 seconds to press Ctrl+C for exiting diSlack or else I\'ll launch Slack for you')
        try:
            time.sleep(3)
        except KeyboardInterrupt:
            print()
            return False
        open_slack(app_options)
    return True


def main(argv):
    app_options = parse_args(argv)
    while True:
        if is_slack_open(app_options):
            logger.info('Slack is running')
            logger.info('Send "/dnd {}m", close Slack, then I start to notify you'.format(app_options.sleep))
        wait_for_slack_closed(app_options)
        need_to_continue = wait_and_notice(app_options)
        if not need_to_continue:
            break

    logger.info('buy')
    return True
