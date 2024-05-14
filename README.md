# Wastu App v1.0 
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/3cbafc90-112d-495c-8fb4-471304b35456" alt="logo">

# A simple Java based Android app with TensorflowLite, SQLite, OpenStreetMap and SharedPreferences
*By Malindu Liyanage

### Screenshots

<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/bbf992f9-1192-42dd-9cfe-18482c72066c" alt="Image 1" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/2032baa3-0bc3-4782-9b1d-72f70531e67e" alt="Image 2" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/24d36bd0-8713-4ef7-a522-61f85271c0d4" alt="Image 3" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/284ba17c-f93c-477d-b5df-9b87d75454fb" alt="Image 4" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/2f7df7a8-3a2e-4e25-a51e-5d806b29f0b0" alt="Image 5" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/fa5aaa3b-0187-49cc-b3e8-dc834e2d1243" alt="Image 6" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/b7ca591d-3bb7-4d59-8659-f7207d7cd973" alt="Image 7" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/82d5481d-8095-470a-9d88-372c38737047" alt="Image 8" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/e99af4be-2719-47de-8375-f3989bab80af" alt="Image 9" width="250" height="600">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/82bb86f5-e92c-4808-aac6-ce6259803b8b" alt="Image 10" width="250" height="520">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/ec6a6d31-221a-4e8e-b190-fb0f5140f261" alt="Image 11" width="250" height="480">
<img src="https://github.com/MalinduLiyanage/Wastu_AI_ObjectDetector_App_v1.0/assets/136006504/678e6607-5383-4524-8021-76eed9e6067d" alt="Image 12" width="250" height="480">

### Overview
The App uses Tensorflow Lite Object detection model based on YOLO v5 to get predictions. 
Basically the model is capable to recognize about 42 object classes.
The app has a built in SQLite DB that stores data within the device. There is no any internet based DB here, all the operations are done in locally.

### Features
1. Uses Tensorflow Lite Object detection model to detect objects.
2. Able to take inputs from Camera and Gallery, and toggle between these modes quickly.
3. Model parameters can be adjusted to get a good prediction.
4. Detections can be saved and see them inside the App.
5. Detections can be compared with the base image.
6. For images captured through Camera, Location data is saved and can be retrieved through the app itself.
7. Uses sharedpreferences to save model parameters.
8. SQLite DB is used to track info about detections.

### References
The pretrained model from this repo is used in this project. <a href="https://github.com/Pawandeep-prog/yolo_android" target="_blank">Click here to visit there</a>

### Last update
2024.05.14

### About
This app is a development assignment for Mobile App development course.
Faculty of Applied Sciences,
Rajarata University of Sri Lanka.



