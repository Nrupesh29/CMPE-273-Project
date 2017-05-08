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
    
3. Go to [AWS IoT Console](https://console.aws.amazon.com/iotv2).

4. Click on Connect in the left navigation bar.

5. Click Get Started for Configuring a device.

6. Choose Linux/OSX under the platform selection and Python for device SDK.

7. Enter the Thing name and click Next Step.

8. Download the connection kit for Linux/OSX.

9. Unzip the connection kit on the device.

10. Add execution permission to the script file in connection kit.
    ```shell
    pi@raspberrypi:~/connect_device_package $ chmod +x start.sh
    ```
    
11. Replace start.sh file with the one in this repository.

12. Execute [start.sh](start.sh) file to Connect to AWS IoT and get Temperature and Humidity data from Data Nodes
    ```shell
    pi@raspberrypi:~/connect_device_package $ ./start.sh
    ```
