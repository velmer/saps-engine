#!/usr/bin/python
# coding: utf-8

import requests

HTTP_SCHEME = 'http://'
LOCALHOST = 'localhost'
SERVER_PORT = 8091

SAPS_INSTANCE_1_IP = '10.11.4.94'
SAPS_INSTANCE_2_IP = '10.11.4.116'

SAPS_INSTANCE_1_URL = HTTP_SCHEME + SAPS_INSTANCE_1_IP + ':' + str(SERVER_PORT)
SAPS_INSTANCE_2_URL = HTTP_SCHEME + SAPS_INSTANCE_2_IP + ':' + str(SERVER_PORT)
LOCAL_URL = HTTP_SCHEME + LOCALHOST + ':' + str(SERVER_PORT)
PROCESSING_TASKS_URN = '/processings'

STATE_KEY = 'state'
ARCHIVED = 'archived'
REMOTELY_ARCHIVED = 'remotely_archived'

LOWER_LEFT_KEY = 'lowerLeft[]'
UPPER_RIGHT_KEY = 'upperRight[]'
INITIAL_DATE_KEY = 'initialDate'
FINAL_DATE_KEY = 'finalDate'
INPUT_GATHERING_KEY = 'inputGatheringTag'
INPUT_PREPROCESSING_KEY = 'inputPreprocessingTag'
ALGORITHM_EXECUTION_KEY = 'algorithmExecutionTag'

LOWER_LEFT_LATITUDE = '-7.913'
LOWER_LEFT_LONGITUDE = '-37.814'
UPPER_RIGHT_LATITUDE = '-6.547'
UPPER_RIGHT_LONGITUDE = '-35.757'
INITIAL_DATE = '2014-06-12'
FINAL_DATE = '2014-06-12'
INPUT_GATHERING = 'Default'
INPUT_PREPROCESSING = 'Default'
ALGORITHM_EXECUTION = 'Default'

USER_EMAIL_KEY = 'userEmail'
USER_PASS_KEY = 'userPass'

ADMIN_EMAIL = 'admin@admin.com'
ADMIN_PASSWORD = '4dm1n'

SUBMIT_PROCESSING_SUCCESSFUL = "Tasks successfully added"


def get_all_image_tasks(saps_instance_url):
    get_all_image_tasks_url = saps_instance_url + PROCESSING_TASKS_URN
    headers = {**get_admin_credentials()}
    response = requests.get(url=get_all_image_tasks_url, headers=headers)
    return response.json()


def submit_processing(saps_instance_url):
    submit_processing_url = saps_instance_url + PROCESSING_TASKS_URN
    data = {**get_admin_credentials(), **get_default_submission_parameters()}
    response = requests.post(url=submit_processing_url, data=data)
    return response.text


def get_admin_credentials():
    return {
        USER_EMAIL_KEY: ADMIN_EMAIL,
        USER_PASS_KEY: ADMIN_PASSWORD
    }


def get_default_submission_parameters():
    return {
        LOWER_LEFT_KEY: [LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE],
        UPPER_RIGHT_KEY: [UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE],
        INITIAL_DATE_KEY: INITIAL_DATE,
        FINAL_DATE_KEY: FINAL_DATE,
        INPUT_GATHERING_KEY: INPUT_GATHERING,
        INPUT_PREPROCESSING_KEY: INPUT_PREPROCESSING,
        ALGORITHM_EXECUTION_KEY: ALGORITHM_EXECUTION
    }


def main():
    image_tasks_instance_1 = get_all_image_tasks(SAPS_INSTANCE_1_URL)
    image_tasks_instance_2 = get_all_image_tasks(SAPS_INSTANCE_2_URL)
    assert len(image_tasks_instance_1) == 1
    assert len(image_tasks_instance_2) == 0

    assert submit_processing(SAPS_INSTANCE_2_URL) == SUBMIT_PROCESSING_SUCCESSFUL

    image_tasks_instance_1 = get_all_image_tasks(SAPS_INSTANCE_1_URL)
    image_tasks_instance_2 = get_all_image_tasks(SAPS_INSTANCE_2_URL)
    assert len(image_tasks_instance_1) == 1
    assert len(image_tasks_instance_2) == 1

    image_task_instance_1 = image_tasks_instance_1[0]
    image_task_instance_2 = image_tasks_instance_2[0]

    assert image_task_instance_1[STATE_KEY] == ARCHIVED
    assert image_task_instance_2[STATE_KEY] == REMOTELY_ARCHIVED
    
    for key in list(image_task_instance_1.keys()):
        if key != STATE_KEY:
            assert image_task_instance_1[key] == image_task_instance_2[key]


if __name__ == "__main__":
    main()

