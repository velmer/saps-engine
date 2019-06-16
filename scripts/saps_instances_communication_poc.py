#!/usr/bin/python
# coding: utf-8

import requests

submission_rest_server_port = 8091

HTTP_SCHEME = 'http://'

LOCALHOST = 'localhost'

SAPS_INSTANCE_1 = '10.11.4.94'
SAPS_INSTANCE_2 = '10.11.4.116'

LOCAL_URL = HTTP_SCHEME + LOCALHOST + ':' + str(submission_rest_server_port)

SAPS_INSTANCE_1_URL = SAPS_INSTANCE_1 + ':' + str(submission_rest_server_port)
SAPS_INSTANCE_2_URL = SAPS_INSTANCE_2 + ':' + str(submission_rest_server_port)

SEARCH_IMAGE_TASKS_URN = '/regions/details'

ARCHIVED = 'archived'
REMOTELY_ARCHIVED = 'remotely_archived'

LOWER_LEFT_LATITUDE = '-7.913'
LOWER_LEFT_LONGITUDE = '-37.814'
UPPER_RIGHT_LATITUDE = '-6.547'
UPPER_RIGHT_LONGITUDE = '-35.757'
INIT_DATE = '2014-06-12'
END_DATE = '2014-06-15'
INPUT_GATHERING = 'Default'
INPUT_PREPROCESSING = 'Default'
ALGORITHM_EXECUTION = 'Default'

admin_email = 'admin@admin.com'
admin_user = 'admin'
admin_password = '4dm1n'


def get_image_tasks_in_catalogue(saps_instance_url):
    search_image_tasks_url = saps_instance_url + SEARCH_IMAGE_TASKS_URN
    data = {**get_admin_credentials(), **get_submission_parameters()}
    response = requests.post(url=search_image_tasks_url, data=data)
    print(response.text)
    return []


def submit_processing(saps_instance_url):
    pass


def get_admin_credentials():
    return {
        'userEmail': admin_email,
        'userPass': admin_password
    }


def get_submission_parameters():
    return {
        'lowerLeft[]': [LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE],
        'upperRight[]': [UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE],
        'initialDate': INIT_DATE,
        'finalDate': END_DATE,
        'inputGatheringTag': INPUT_GATHERING,
        'inputPreprocessingTag': INPUT_PREPROCESSING,
        'algorithmExecutionTag': ALGORITHM_EXECUTION
    }


def main():
    get_image_tasks_in_catalogue(LOCAL_URL)
    # image_tasks_instance_1 = get_image_tasks_in_catalogue(SAPS_INSTANCE_1_URL)
    # image_tasks_instance_2 = get_image_tasks_in_catalogue(SAPS_INSTANCE_2_URL)
    # assert len(image_tasks_instance_1) == 1
    # assert len(image_tasks_instance_2) == 0
    #
    # submit_processing(SAPS_INSTANCE_2)
    #
    # image_tasks_instance_1 = get_image_tasks_in_catalogue(SAPS_INSTANCE_1_URL)
    # image_tasks_instance_2 = get_image_tasks_in_catalogue(SAPS_INSTANCE_2_URL)
    # assert len(image_tasks_instance_1) == 1
    # assert len(image_tasks_instance_2) == 1
    #
    # image_task_instance_1 = image_tasks_instance_1[0]
    # image_task_instance_2 = image_tasks_instance_2[0]
    #
    # assert image_task_instance_1.state == ARCHIVED
    # assert image_task_instance_2.state == REMOTELY_ARCHIVED


if __name__ == "__main__":
    main()
    # print requests.get(LOCAL_URL + SEARCH_IMAGE_TASKS_URN).text
    # print requests.post(LOCAL_URL + SEARCH_IMAGE_TASKS_URN, data={'userEmail': 'email', 'userPass': 'pass', 'lowerLeft[]': ['LLLat', 'LLLon'], 'upperRight[]': ['URLat', 'URLon'], 'initialDate': '2019-06-12', 'finalDate': '2019-07-01', 'inputGatheringTag': 'Default', 'inputPreprocessingTag': 'Default', 'algorithmExecutionTag': 'Default'}).text
