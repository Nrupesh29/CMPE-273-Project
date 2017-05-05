# Data Node

## Connection Diagram

<p align="center"><img src="http://nrupeshpatel.com/CMPE273/Images/dht11Connection.png" width="40%" /></p>

<p align="center">
     3.3v P1    --------    VCC (V) </br>
      GND P6    --------    GND (G) </br>
    GPIO4 P7    --------    DATA (S) </br>
</p>

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
3. Clone [pigpio](https://github.com/joan2937/pigpio) library to control the General Purpose Input Outputs (GPIO)

    ```shell
    pi@raspberrypi:~$ git clone https://github.com/joan2937/pigpio.git
    ```
4. Run the pigpiod daemon

    ```shell
    pi@raspberrypi:~$ sudo pigpiod
    ```
5. Execute [dht11.py](dht11.py) file to get Temperature and Humidity data from Raspberry Pi and publish it to channel

    ```shell
    pi@raspberrypi:~$ sudo python dht11.py
    ```
