<p align="center"><img src="http://nrupeshpatel.com/CMPE273/Images/logo.png" width="40%" /></p>

<h1 align="center">Raspberry Pi Fog Network</h1>
<p align="center">
<a href="https://github.com/SJSU272Lab/DrugWatch/blob/master/LICENSE.md" rel="Licence"><img src="https://img.shields.io/github/license/mashape/apistatus.svg" /></a>
<a href="" rel="Platform"><img src="https://img.shields.io/badge/platform-IoT-orange.svg" /></a>
<a href="https://github.com/SJSU272Lab/DrugWatch/issues?q=is%3Aopen+is%3Aissue" rel="GitHub issues"><img src="https://img.shields.io/badge/issues-0%20open-green.svg" /></a>
<a href="https://github.com/SJSU272Lab/DrugWatch/issues?q=is%3Aissue+is%3Aclosed" rel="GitHub closed issues"><img src="https://img.shields.io/badge/issues-0%20closed-red.svg" /></a>
</p>
<table>
<tr>
<td>
<p align="center">Transfer Temperature and Humidity Data from Fog Nodes to Server Node. Data reduction rules implemented on Server Node. Server node sends processed and relevant data to the Cloud.</p>
</td>
</tr>
</table>

## Table of content

- [Architecture Flow Diagram](#architecture-flow-diagram)
- [API](#api)
- [Services](#services)
- [License](#license)
- [Team Members](#team-members)

## Architecture Diagram
<p align="center"><img src="http://nrupeshpatel.com/CMPE273/Images/arch.png" /></p>

## Workflow

1. Initialize Fog Nodes (Raspberry Pi with Sensors).

2. Start the Server Node (Raspberry Pi connected to Cloud).

3. Server node receives temperature and humidity data from Fog nodes every second.

4. Server node processes the data locally.

5. Relevant and processed data is sent to the Cloud (AWS IoT).

6. AWS IoT stores the data into DynamoDB.

7. User registers on the Mobile application using following details:

    - Mobile Number
    - Email Address
    - Alert Threshold 

8. Alerts are sent using AWS SNS (Email) and Twilio API (SMS).

## API

### Temperature and Humidity API Calls

| Request | Endpoint      |Description                 |
|---------|--------------|------------------------------------------|
| GET     | `{base_url}/readings` | List latest sensor data for all devices |
| GET     | `{base_url}/reading/{device_id}` | List latest sensor data for device with id **`device_id`** |

## Services

- AWS IoT
- AWS DynamoDB
- AWS API Gateway
- AWS SNS
- AWS Lambda
- Twilio API
- MQTT Protocol

## License

Raspberry Pi Fog Network is released under the [MIT License](https://github.com/SJSU272Lab/Fall16-Team11/blob/master/LICENSE.md).

## Team Members

| [![Nrupesh Patel](https://avatars.githubusercontent.com/nrupesh29?s=100)<br /><sub>Nrupesh Patel</sub>](https://github.com/Nrupesh29)<br /> | [![Nam Phan](https://avatars.githubusercontent.com/mostman47?s=100)<br /><sub>Nam Phan</sub>](https://github.com/mostman47)<br /> | [![Aditi Shetty](https://avatars.githubusercontent.com/shettyaditi?s=100)<br /><sub>Aditi Shetty</sub>](https://github.com/shettyaditi)<br />| [![Suchita Mudholkar](https://avatars.githubusercontent.com/suchitaM?s=100)<br /><sub>Suchita Mudholkar</sub>](https://github.com/suchitaM)<br />|
| :---: | :---: | :---: | :---: |
