# Server Node

## Setup Instructions

1. Install Mosquitto MQTT server
    ```shell
    pi@raspberrypi:~$ sudo apt-get install mosquitto
    ```
2. Install [python libraries](https://pypi.python.org/packages/83/96/dacc2b78bc9c5cd83eed178e9ce35d7bceecf2dd38db079c0190423efd4a/paho-mqtt-1.1.tar.gz) to create a link between Python and MQTT
    ```shell
    pi@raspberrypi:~$ tar xvf paho-mqtt-1.1.tar.gz
    pi@raspberrypi:~$ cd paho-mqtt-1.1
    pi@raspberrypi:~/paho-mqtt-1.1 $ sudo python setup.py install
    ```
3. Execute [server.py](server.py) file to get Temperature and Humidity data from Data Nodes
    ```shell
    pi@raspberrypi:~$ sudo python server.py
    ```
