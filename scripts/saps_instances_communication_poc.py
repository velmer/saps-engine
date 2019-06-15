#!/usr/bin/python
# coding: utf-8

import requests

SAPS_INSTANCE_1 = '10.11.4.94'
SAPS_INSTANCE_2 = '10.11.4.116'

SEARCH_IMAGE_TASKS_URN = '/regions/search'

ARCHIVED = 'archived'
REMOTELY_ARCHIVED = 'remotely_archived'

LOWER_LEFT_LATITUDE = '-7.913'
LOWER_LEFT_LONGITUDE = '-37.814'
UPPER_RIGHT_LATITUDE = '-6.547'
UPPER_RIGHT_LONGITUDE = '-35.757'
INIT_DATE = '2014-06-12'
END_DATE = '2014-06-12'
INPUT_GATHERING = 'Default'
INPUT_PREPROCESSING = 'Default'
ALGORITHM_EXECUTION = 'Default'


def get_image_tasks_in_catalogue(saps_instance_url):
    search_image_tasks_url = saps_instance_url + SEARCH_IMAGE_TASKS_URN
    requests.post(search_image_tasks_url)
    return []


def submit_processing(saps_instance_url):
    pass


def create_default_user():
    pass


def main():
    create_default_user()

    image_tasks_instance_1 = get_image_tasks_in_catalogue(SAPS_INSTANCE_1)
    image_tasks_instance_2 = get_image_tasks_in_catalogue(SAPS_INSTANCE_2)
    assert len(image_tasks_instance_1) == 1
    assert len(image_tasks_instance_2) == 0
    
    submit_processing(SAPS_INSTANCE_2)

    image_tasks_instance_1 = get_image_tasks_in_catalogue(SAPS_INSTANCE_1)
    image_tasks_instance_2 = get_image_tasks_in_catalogue(SAPS_INSTANCE_2)
    assert len(image_tasks_instance_1) == 1
    assert len(image_tasks_instance_2) == 1

    image_task_instance_1 = image_tasks_instance_1[0]
    image_task_instance_2 = image_tasks_instance_2[0]

    assert image_task_instance_1.state == ARCHIVED
    assert image_task_instance_2.state == REMOTELY_ARCHIVED

if __name__ == "__main__":
    main()
