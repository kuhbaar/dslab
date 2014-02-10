This application is used to simulate a distributed system with clients, proxy and fileservers.

Ant Template
=============

Ant is a Java-based build tool that significantly eases the development process. If you have not installed ant yet, download it and follow the instructions.
We provide a template build file (build.xml in the template) in which you only have to adjust some parameters and class names. Put your source into the subdirectory "src/main/java". To compile your code, simply type "ant" in the directory where the build file is located. Enter "ant run-proxy" to start the Proxy, "ant run-client" to start the client and "ant run-fileserverX" (with X being 1 or 2) to start the respective fileserver. Note that it's absolutely required that we are able to start your programs with these predefined commands! Also note that build files created by IDE's like Netbeans very often aren't portable, so please use the provided template.

Usage
==========

Open terminals for each a client, proxy and 2 fileservers

Start proxy - "ant run-proxy"

Start client - "ant run-client"

Start fileserver - "ant run-fileserver1"

start 2nd fileserver - "ant run-fileserver2"

The architecture is as follows: Fileservers store all the files available to the clients. They are fully replicated, i.e., each single fileserver stores exactly the same files. Files are identified by their filename and all reside in a given directory of the file system.

At any point of communication, the clients only know the address of one particular server. This server stores no files at all, but forwards any incoming download request to one of the available fileservers. Due to this task, we will call this server 'Proxy' in the following.

The Proxy stores information about every client and every fileserver in the communication process. Clients are limited in the amount of data they are allowed to download. Because it is forwarding each client request and each fileserver's response, the Proxy can easily keep track of the user's current limit and block download requests where necessary. The same approach is used to balance the upcoming traffic in the private fileserver network: The fileserver to choose for responding to the next client request is always the one with the lowest usage at that time. Figure 1 illustrates a simple example with two clients downloading a file (client1, client2) and one client uploading a file (client3). For downloads, the proxy returns a "download ticket", which contains all necessary information for the client to contact the responsible file server directly. The figure also illustrates how the user credits decrease (UPDATE 17.10.13 after downloading a file after requesting a download ticket) and increase (after uploading a file) - note that upload credits should be worth double the amount of download credits (for simplicity, we assume the credits are based on the file size), in order to motivate users to upload files.


To avoid a waste of network resources, there is no connection being held between the Proxy and a fileserver between two distinct requests. That is, after the fileserver has responded to the Proxy’s request, the connection gets closed again. However, to signal that it is still online and ready to handle requests, a fileserver needs to send UDP messages (so called "isAlive" packets) in a recurring manner – any other communication in this assignment is done using TCP. Figure 2 illustrates this behavior: Imagine that in the example above, ‘fileserver1’ would fail to send alive packets to the Proxy. The Proxy will remove ‘fileserver1’ from its list of available fileservers and instead forward the next download request to ‘fileserver3’, which now is the least used fileserver.


Client Interactive commands
!login <username> <password>
Log in the user. Before the user hasn’t successfully logged in, this is the only command that will be executed by the Proxy.
E.g.:
>: !login alice 23456\\
Wrong username or password.
>: !login alice 12345\\
Successfully logged in.
!credits
Requests the user’s current amount of credits. Requires a successfully logged in user.
E.g.:
>: !credits
You have 500 credits left.
!buy <credits>
Allows the user to increase his/her amount of credits. Requires a successfully logged in user.
E.g.:
>: !credits
You have 500 credits left.
>: !buy 1000
You now have 1500 credits.
!list
Gets the complete list of files available to download. Requires a successfully logged in user.
E.g.:
>: !list
file1.txt
file2.txt
!upload <filename>
Uploads the specified file to the Proxy, which takes care of replicating the file to all file servers. Furthermore, the Proxy increases the uploading user's credit count accordingly.
E.g.:
>: !upload file1.txt
File successfully uploaded.
You now have 1600 credits.
!download <filename>
Downloads the specified file into the private download folder (downloadDir).
E.g.:
>: !download file1.txt
File successfully downloaded.
!logout
Log out the currently logged in user, and drop any state information from memory that the client has associated with this user.
!exit
Shutdown the client: Logout the user if necessary and be sure to release all resources, stop all threads and close any open sockets.

Finally, the Proxy accepts the following interactive commands:

!fileservers
Prints out some information about each known fileserver, online or offline. A fileserver is known if it has sent a single isAlive packet since the Proxy's last startup. The information shall contain the fileserver's IP, TCP port, online status (online/offline) and usage.
E.g.:
>: !fileservers
1. IP:127.0.0.1 Port:10000 offline Usage: 752
2. IP:127.0.0.2 Port:10000 online Usage: 220
!users
Prints out some information about each user, containing username, login status (online/offline) and credits.
E.g.:
>: !users
1. alice online Credits: 500
2. bill offline Credits: 180
!exit
Shutdown the Proxy. 
