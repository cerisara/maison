
import socket                   # Import socket module

port = 38634                    # Reserve a port for your service every new transfer wants a new port or you must wait.
s = socket.socket()             # Create a socket object
host = ""   # Get local machine name
s.bind((host, port))            # Bind to the port
s.listen(5)                     # Now wait for client connection.

print 'Server listening....'


while True:
    conn, addr = s.accept()     # Establish connection with client.
    print 'Got connection from', addr

    with open('received_file', 'wb') as f:
        print 'file opened'
        while True:
            print('receiving data...')
            data = conn.recv(1024)
            if not data: break
            # write data to a file
            f.write(data)
    f.close()
    print('Successfully get the file')

#    filename='TCPSERVER.py' #In the same folder or path is this file running must the file you want to tranfser to be
#    f = open(filename,'rb')
#    l = f.read(1024)
#    while (l):
#       conn.send(l)
#       print('Sent ',repr(l))
#       l = f.read(1024)
#    f.close()

    conn.close()

