#!/usr/bin/python
# coding: utf-8

import requests

# HTTP protocol scheme
HTTP_SCHEME = 'http://'
# SAPS instances' server port
SERVER_PORT = 8091

# IP of Instance 1
SAPS_INSTANCE_1_IP = '10.11.4.94'
# IP of Instance 2
SAPS_INSTANCE_2_IP = '10.11.4.116'

# URL of Instance 1
SAPS_INSTANCE_1_URL = HTTP_SCHEME + SAPS_INSTANCE_1_IP + ':' + str(SERVER_PORT)
# URL of Instance 2
SAPS_INSTANCE_2_URL = HTTP_SCHEME + SAPS_INSTANCE_2_IP + ':' + str(SERVER_PORT)

# URN (API route) of submissions
PROCESSING_TASKS_URN = '/processings'

# Initial quantity of ImageTasks stored in catalog of instance 1, which must
# have 3 ImageTasks, all of them for date 2014-06-12 and one for each satellite
# (Landsat 5, Landsat 7 e Landsat 8)
QTY_IMAGE_TASKS_INSTANCE_1 = 3
# Initial quantity of ImageTasks stored in catalog of instance 2, which must
# have 0 ImageTasks
QTY_NO_IMAGE_TASKS = 0

# Key of ID attribute of ImageTask
ID_ATTR_KEY = 'taskId'
# Key of State attribute of ImageTask
STATE_ATTR_KEY = 'state'

# Values of ImageTask's State property
ARCHIVED = 'archived'
REMOTELY_ARCHIVED = 'remotely_archived'

# Keys of SubmissionParameters
LOWER_LEFT_KEY = 'lowerLeft[]'
UPPER_RIGHT_KEY = 'upperRight[]'
INITIAL_DATE_KEY = 'initialDate'
FINAL_DATE_KEY = 'finalDate'
INPUT_GATHERING_KEY = 'inputGatheringTag'
INPUT_PREPROCESSING_KEY = 'inputPreprocessingTag'
ALGORITHM_EXECUTION_KEY = 'algorithmExecutionTag'

# Values of SubmissionParameters
LOWER_LEFT_LATITUDE = '-7.913'
LOWER_LEFT_LONGITUDE = '-37.814'
UPPER_RIGHT_LATITUDE = '-6.547'
UPPER_RIGHT_LONGITUDE = '-35.757'
INITIAL_DATE = '2014-06-12'
FINAL_DATE = '2014-06-12'
INPUT_GATHERING = 'Default'
INPUT_PREPROCESSING = 'Default'
ALGORITHM_EXECUTION = 'Default'

# Keys of auth credentials
USER_EMAIL_KEY = 'userEmail'
USER_PASS_KEY = 'userPass'

# Values of auth credentials
ADMIN_EMAIL = 'admin@admin.com'
ADMIN_PASSWORD = '4dm1n'

# Successful feedback of submission processing
SUBMIT_PROCESSING_SUCCESSFUL = "Tasks successfully added"


def get_all_image_tasks(saps_instance_url):
    """
    Returns all ImageTasks stored in SAPS instance that had its URL specified.

    :param saps_instance_url: URL of SAPS instance.
    :return: List of all ImageTasks in SAPS instance's catalog.
    """
    get_all_image_tasks_url = saps_instance_url + PROCESSING_TASKS_URN
    headers = {**get_admin_credentials()}
    response = requests.get(url=get_all_image_tasks_url, headers=headers)
    return response.json()


def submit_processing(saps_instance_url):
    """
    Submits a processing with default SubmissionParameters to the SAPS instance
    that had its URL specified.

    :param saps_instance_url: URL of SAPS instance.
    :return: Feedback of submission processing.
    """
    submit_processing_url = saps_instance_url + PROCESSING_TASKS_URN
    data = {**get_admin_credentials(), **get_default_submission_parameters()}
    response = requests.post(url=submit_processing_url, data=data)
    return response.text


def get_admin_credentials():
    """
    :return: admin credentials for both SAPS instances.
    """
    return {
        USER_EMAIL_KEY: ADMIN_EMAIL,
        USER_PASS_KEY: ADMIN_PASSWORD
    }


def get_default_submission_parameters():
    """
    :return: default SubmissionParameters.
    """
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
    # Test preconditions
    image_tasks_instance_1 = get_all_image_tasks(SAPS_INSTANCE_1_URL)
    image_tasks_instance_2 = get_all_image_tasks(SAPS_INSTANCE_2_URL)
    assert len(image_tasks_instance_1) == QTY_IMAGE_TASKS_INSTANCE_1
    assert len(image_tasks_instance_2) == QTY_NO_IMAGE_TASKS

    # Submits processing to Instance 2
    assert submit_processing(SAPS_INSTANCE_2_URL) == SUBMIT_PROCESSING_SUCCESSFUL

    # Instance 2 must have inserted each ImageTask stored in Instance 1
    image_tasks_instance_1 = get_all_image_tasks(SAPS_INSTANCE_1_URL)
    image_tasks_instance_2 = get_all_image_tasks(SAPS_INSTANCE_2_URL)
    assert len(image_tasks_instance_1) == QTY_IMAGE_TASKS_INSTANCE_1
    assert len(image_tasks_instance_2) == QTY_IMAGE_TASKS_INSTANCE_1

    # Sorts lists of two instances to compare their elements as pairs
    sorted(image_tasks_instance_1, key=lambda image_task: image_task[ID_ATTR_KEY])
    sorted(image_tasks_instance_2, key=lambda image_task: image_task[ID_ATTR_KEY])

    for i in range(QTY_IMAGE_TASKS_INSTANCE_1):
        image_task_instance_1 = image_tasks_instance_1[i]
        image_task_instance_2 = image_tasks_instance_2[i]

        # Reused ImageTasks must have the 'remotely_archived' state
        assert image_task_instance_1[STATE_ATTR_KEY] == ARCHIVED
        assert image_task_instance_2[STATE_ATTR_KEY] == REMOTELY_ARCHIVED

        # Except for the State, every other attribute of reused ImageTasks
        # must be equal to processed ImageTasks, including the ID
        for attr_key in list(image_task_instance_1.keys()):
            if attr_key != STATE_ATTR_KEY:
                assert image_task_instance_1[attr_key] == image_task_instance_2[attr_key]


if __name__ == "__main__":
    main()

