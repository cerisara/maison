import socket                   # Import socket module

s = socket.socket()             # Create a socket object
host = "cerisara.duckdns.org"  #Ip address that the TCPServer  is there
port = 38634                     # Reserve a port for your service every new transfer wants a new port or you must wait.

s.connect((host, port))
s.send("Hello server!")
s.close()

