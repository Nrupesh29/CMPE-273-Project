# Data Node

## Setup Instructions

1. Clone [pigpio](https://github.com/joan2937/pigpio) library to control the General Purpose Input Outputs (GPIO)

    ```shell
    pi@raspberrypi:~$ git clone https://github.com/joan2937/pigpio.git
    ```
2. Run the pigpiod daemon

    ```shell
    pi@raspberrypi:~$ sudo pigpiod
    ```
3. Execute [dht11.py](dht11.py) file to get Temperature and Humidity data from Raspberry Pi

    ```shell
    pi@raspberrypi:~$ sudo python dht11.py
    ```