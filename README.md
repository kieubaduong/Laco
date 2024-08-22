# Laptop Controller Application

https://github.com/user-attachments/assets/7461ea61-afc3-4d07-a150-f4c30dc56338

## Description

Laco is an Android mobile app that allows you to control your laptop remotely. It collaborates with the LacoService repository, which runs as a service on your Windows laptop.

## How to Use
1. Clone the repository.
2. Open the project in Android Studio.
3. Change the value YOUR_MACHINE_IP to the IP address of your Windows laptop using --ipconfig.
4. Clone the LacoService repository.
5. Open powershell or command prompt as an administrator (need to run as an administrator to be able to control applications running as an administrator privileges).
6. Run the following command in the LacoService repository:
```
cd to the LacoService directory
.\gradlew shadowJar
java -jar .\build\libs\LacoService-1.0-SNAPSHOT-all.jar
```
7. Connect to the same network as your Windows laptop.
8. Run the app on your Android device.
9. Enjoy! 
