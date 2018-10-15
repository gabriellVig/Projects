#include <stdio.h>
#include <stdlib.h>

#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <signal.h>

#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <sys/types.h>
#include <sys/wait.h>

//Sizes
#define size_char  1
#define size_int 4

//Defines for job bits
#define job_1 1
#define job_n 2
#define job_all 4
#define quit 8
#define error 16

//defines for type
#define type_shift 5
#define type_O 0
#define type_E 1
#define type_QUIT 7
#define checksum_shift 3

#define protocol_size 4

void stopsleep(){}//ONLY to stop the sleep() call in method sendToChildren when child is finished printing.

//Method declarations. Method explanations and content found after main.
void sendToChildren(unsigned char type, int size, unsigned char* string_to_children);
int getIP(char* hostname, char **ip);
void ctrl_c_handler();
void ctrl_c_handler_children();
void killAndWait();
void closePipes();
void send_to_Server();
void printMainOptions();
int getNumberFromUser();




int sock;//Socket for connecting
int pipe1[2], pipe2[2];//pipes to child processes
int connected = 0;//Is set to 1 upon connection success
unsigned char protocol[protocol_size];//For the protocol
unsigned char* newbuffer = NULL;

int main(int argn, char* argv[]){
  //ARGN TEST START
  if(argn != 3){
    fprintf(stdout, "ERROR: Correct usage: ./program_name <port_number(1025...n)> <hostname OR ip.adress\n");
    exit(0);
  }
  int portnumber = atoi(argv[2]);
  if(portnumber < 1024){
    fprintf(stdout, "ERROR: portnumber < 1024 OR '%s' is not a number.\nExiting.\n", argv[2]);
    exit(0);
  }
  char* hostname = argv[1];
  //hostname = "129.240.68.147";  = brederode.ifi.uio.no; PC in the library at Ole Johan Dahl's Hus
  //ARGN TEST END

  //PIPE TEST START
  if (pipe(pipe1) < 0) {
    perror("pipe1 call error");
    exit(EXIT_FAILURE);
  }
  if (pipe(pipe2) < 0) {
    perror("pipe2 call error");
    exit(EXIT_FAILURE);
  }
  //PIPE TEST END

  //Declarations
  pid_t pid1, pid2;
  struct sockaddr_in serveraddr;
  unsigned int size = 1;

  signal(SIGUSR1, stopsleep);//Signal to be used from children to parent.

  pid1 = fork();
  switch(pid1) {
    case -1:
      fprintf(stdout, "Error from fork call 1: %s\n", strerror(errno));
      closePipes();
      exit(2);
    case 0:
      //Child nr1 START (O)
      signal(SIGINT, ctrl_c_handler_children);
      int totalread = 0;
      while(size != 0){//While it hasn't recieved size = 0, run.
        read(pipe1[0], &size, size_int);
        if(size == 0){//to stop it from reading text when size is 0
          break;
        }
        unsigned char* newbuffer = malloc(size +1);//malloc newbuffer
        newbuffer[size] = '\0';
        read(pipe1[0], newbuffer, size);
        #ifdef DEBUG
          fprintf(stdout, ">>> %u <<< (C) Type: O. Size: %u.\n\n" ,getpid(),size);//only type o printed from here
        #endif
        #ifndef DEBUG
          fprintf(stdout, ">>> %u <<< (C) \n%s\n",getpid(), newbuffer);
        #endif
        free(newbuffer);//free newbuffer
        kill(getpid()-1, SIGUSR1);//Send signal to Parent to interrupt wait() call in method sendToChildren()
        totalread++;
      }
      fprintf(stdout, ">>> %u <<< (C) Read a total of %u jobs\n",getpid(), totalread);
      fprintf(stdout, ">>> %u <<< (C) Exiting\n",getpid());
      exit(0);
      //Child nr1 END (O)
    default:
      pid2 = fork();//Forking to make another child process
      switch(pid2){
        case -1:
          fprintf(stdout, "Error from fork call 2: %s\n", strerror(errno));
          closePipes();
          exit(2);

        case 0:
        //CHILD NR 2 (E) START
        //Identical to child 1, but using pipe2 instead of pipe1 and printing type E instead of O
        signal(SIGINT, ctrl_c_handler_children);
        int totalread = 0;
        while(size != 0){
          read(pipe2[0], &size, size_int);
          if(size == 0){
            break;
          }
          unsigned char* newbuffer = malloc(size +1);
          newbuffer[size] = '\0';
          read(pipe2[0], newbuffer, size);
          #ifdef DEBUG
            fprintf(stdout, ">>> %u <<< (C) Type: E. Size: %u.\n\n" ,getpid(),size);
          #endif
          #ifndef DEBUG
            fprintf(stdout, ">>> %u <<< (C) \n%s\n",getpid(), newbuffer);
          #endif
          free(newbuffer);
          kill(getpid()-2, SIGUSR1);
          totalread++;
        }
        fprintf(stdout, ">>> %u <<< (C) Read a total of %u jobs\n",getpid(), totalread);
        fprintf(stdout, ">>> %u <<< (C) Exiting\n",getpid());
        exit(0);
        //CHILD NR 2 (E) END

        default://Parent
          signal(SIGINT, ctrl_c_handler);//signal handler for parent

          sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);//making socket
          memset(&serveraddr, 0, sizeof(serveraddr));//zero's out the serveradress struct
          serveraddr.sin_family = AF_INET;//Set the domain to internet
          char* ip = "";//for getting ip from hostname
          if(atoi(hostname) != 0){
              ip = hostname;
          }
          else if(getIP(hostname, &ip) != 0){
            fprintf(stdout, ">>> %u <<< (P) Could not resolve hostname %s.\n", getpid(), hostname);
            size = 0;
            write(pipe1[1], &size, size_int);
            write(pipe2[1], &size, size_int);
            #ifdef DEBUG
              fprintf(stdout, ">>> %u <<< (P) Waiting for children to stop.\n", getpid());
            #endif
            wait(0);
            closePipes();
            close(sock);
            fprintf(stdout, ">>> %u <<< (P) Exiting.\n", getpid());
            exit(0);
          }
          serveraddr.sin_addr.s_addr = inet_addr(ip);
          serveraddr.sin_port = htons(portnumber);//set portnumber
          int size;
          if(connect(sock, (struct sockaddr *)&serveraddr, sizeof(serveraddr))){//connect to client
            fprintf(stdout, ">>> %u <<< (P) Could not connect. Error: %s\n", getpid(), strerror(errno));
            size = 0;
            write(pipe1[1], &size, size_int);
            write(pipe2[1], &size, size_int);
            #ifdef DEBUG
              fprintf(stdout, ">>> %u <<< (P) Waiting for children to stop.\n", getpid());
            #endif
            wait(0);
            closePipes();
            close(sock);
            fprintf(stdout, ">>> %u <<< (P) Exiting.\n", getpid());
            exit(0);
          }
          connected = 1;//is connected = true
          int quit_message_recieved = 0;
          unsigned char type;

          //Declarations
          int inner_index, calc_chck_index, checksum_calculated;
          unsigned char checksum_received;
          int amount_jobs_from_server, all_jobs = 0, user_input;

          while(0 < 1){//forever, using breaks to stop.
            memset(protocol, '\0', sizeof(protocol));
            amount_jobs_from_server = 1;
            all_jobs = 0;
            printMainOptions();
            user_input = 5;

            while(user_input > 4){//user has to type valid number
              fprintf(stdout, "Type a number from 1-4.\n");
              user_input = getNumberFromUser();
              #ifdef DEBUG
                fprintf(stdout, "You wrote: %u\n", user_input);
              #endif
            }

            if(user_input == 1){//1 job
              protocol[3] |= job_1;
            }
            else if(user_input == 2){//n amount of jobs
              fprintf(stdout, "How many jobs do you want to get?\n");
              amount_jobs_from_server = getNumberFromUser();//getting number of jobs from user
              protocol[0] = (amount_jobs_from_server >> 16) & 0xFF;//Bitshifting to fit n (0 ... 2^24-1) inside 3 bytes
              protocol[1] = (amount_jobs_from_server >> 8) & 0xFF;//Make sure to unravel this in the correct order
              protocol[2] = amount_jobs_from_server & 0xFF;//and with the correct shifts on the other side
              protocol[3] |= job_n;//Setting type
            }
            else if(user_input == 3){//all the jobs
              protocol[3] |= job_all;//Setting type
              all_jobs = 1;//Set to true, used in for loop below to send to client.
            }
            else if(user_input == 4){//quit
              protocol[3] |= quit;
              send_to_Server();//Not sending with protocol because it is global.
              break;
            }
            send_to_Server();//Sending the protocl to the server

            #ifdef DEBUG
              fprintf(stdout, ">>> %u <<< (P) Jobs from server: %u.\n",getpid(), amount_jobs_from_server);
            #endif
            for(inner_index = 0; inner_index < amount_jobs_from_server || all_jobs == 1; inner_index++){
//READ TYPE START #
              size_t recieved_bytes = 0;//amount received
              int recieving_size = 1;//how much i expect to recieve
              #ifdef DEBUG
                fprintf(stdout, ">>> %u <<< (P) Reading type from socket.\n",getpid());
              #endif
              while(((recieved_bytes = read(sock, &type, recieving_size)) > 0) && (recieving_size > 0)){//type og checksum
                recieving_size -= recieved_bytes;
                if(recieving_size == 0){//if the amount i recieved is equal to the amount i expected to receive
                  break;
                }
                #ifdef DEBUG
                fprintf(stdout, ">>> %u <<< (P) Client read [%d] bytes. Remaining bytes: [%d].\n",getpid(), (int) recieved_bytes, recieving_size);
                #endif
              }
              #ifdef DEBUG
                fprintf(stdout, ">>> %u <<< (P) Reading type from socket successful.\n",getpid());
              #endif
//READ TYPE END %

//READ SIZE START #
              #ifdef DEBUG
                fprintf(stdout, ">>> %u <<< (P) Reading size from socket.\n",getpid());
              #endif
              recieving_size = size_int;//What i expect to receive (4 bytes)
              while(((recieved_bytes = read(sock, protocol, recieving_size)) > 0) && recieving_size > 0){//size
                recieving_size -= recieved_bytes;
                if(recieving_size == 0){
                  break;
                }
                #ifdef DEBUG
                  fprintf(stdout, ">>> %u <<< (P) Client read [%d] bytes. Remaining bytes: [%d].\n",getpid(), (int) recieved_bytes, recieving_size);
                #endif
              }
              #ifdef DEBUG
                fprintf(stdout, ">>> %u <<< (P) Reading size from socket successful.\n",getpid());
              #endif
              size = (protocol[0] << 24) + (protocol[1] << 16) + (protocol[2] << 8) + (protocol[3]);
//READ SIZE END %

              if((type >> type_shift) == type_QUIT){//if the server doesnt have more to read/sent quit message
                quit_message_recieved = 1;//to stop both loops
                break;
              }

//READ TEXT START #
              newbuffer = (unsigned char*) malloc(size);//Allocating space for the text. malloc newbuffer
              unsigned char* read_text_buff = (unsigned char*) malloc(size);//A temp buffer to put what is read. malloc read_text_buff
              int newbuffer_index = 0, read_text_buff_index = 0;//indexes for each buffer
              memset(newbuffer, '\0', size);
              memset(read_text_buff, '\0', size);

              #ifdef DEBUG
                fprintf(stdout, ">>> %u <<< (P) Reading text from socket.\n",getpid());
              #endif
              recieving_size = size;

              /* The while loop runs until all the data has been read. I had a problem with data not
              *  being available instantanously (Weird right?/s) So I had to make a temporary buffer
              *  where i could place what has been read, then transfer that to the buffer which actually
              *  contains the text, and then continue doing that until everything has been read. */
              while(((recieved_bytes = read(sock, read_text_buff, recieving_size)) > 0) && recieving_size > 0){//text
                recieving_size -= recieved_bytes;
                #ifdef DEBUG
                  fprintf(stdout, ">>> %u <<< (P) Client read [%d] bytes. Remaining bytes: [%d]. Buffer size: %u\n",getpid(), (int) recieved_bytes, recieving_size, (int) strlen((const char*)newbuffer));
                #endif
                //For loop to put
                for(read_text_buff_index = 0; read_text_buff_index < (int) recieved_bytes; newbuffer_index++, read_text_buff_index++){
                  newbuffer[newbuffer_index] = read_text_buff[read_text_buff_index];//To copy the content from temporary buffer to the main buffer
                }
                if(recieving_size == 0){
                  break;
                }
              }
              free(read_text_buff);//free read_text_buff
              #ifdef DEBUG
                fprintf(stdout, ">>> %u <<< (P) Reading text from socket successful.\n",getpid());
              #endif
//READ TEXT END %

              if((type >> type_shift) == type_QUIT){//if the server doesnt have more to read/sent quit message
                quit_message_recieved = 1;//to stop both loops
                break;
              }

//CALCULATE AND READ CHECKSUM START #
              checksum_calculated = 0;
              checksum_received = 0;
              for(calc_chck_index = 0; calc_chck_index < size; calc_chck_index++){
                checksum_calculated += newbuffer[calc_chck_index];
              }
              checksum_calculated %= 32;
              checksum_received |= type << checksum_shift;//For å fjerne 3 høyeste bits
              checksum_received = checksum_received >> checksum_shift;//
//CALCULATE AND READ CHECKSUM END  %

              #ifdef DEBUG//Printing some info if the debug mode is on
                if((type >> type_shift) == (int) type_O){
                  fprintf(stdout, ">>> %u <<< (P) Type: O. Size: %u. Checksum calculated: %u. Checksum recieved: %u.\n",getpid(), size, checksum_calculated, checksum_received);
                } else if((type >> type_shift) == (int) type_E){
                  fprintf(stdout, ">>> %u <<< (P) Type: E. Size: %u. Checksum calculated: %u. Checksum recieved: %u.\n",getpid(), size, checksum_calculated, checksum_received);
                }
              #endif

              if(checksum_received == checksum_calculated){//If the checksum is correct, send onwards to children
                sendToChildren(type, size, newbuffer);
              }else{
                fprintf(stdout, ">>> %u <<< (P) Calculated checksum %u != %u Received Checksum.\n",getpid(), checksum_calculated, checksum_received);
              }

              free(newbuffer);//free newbuffer
              sleep(1);//To make sure the text is printed in the correct order.
              //When a child process is finished printing, a signal is sent to interrupt this sleep.
            }

            if(quit_message_recieved == 1){ //breaking out of outer loop
              fprintf(stdout, ">>> %u <<< (P) Quit message recieved from server. No more jobs to read.\n", getpid());
              break;
            }
          }

          fprintf(stdout, ">>> %u <<< (P) Sending quit message to server\n", getpid());
          memset(protocol, '\0', sizeof(protocol));
          protocol[3] |= quit;
          send_to_Server();//Not sending with protocol because it is global.

          size = 0;//to kill the children
          write(pipe1[1], &size, size_int);//sending with size = 0 stops the loop in the child process
          write(pipe2[1], &size, size_int);

          wait(0);//wait for child processes to die
          fprintf(stdout, ">>> %u <<< (P) Exiting\n", getpid());
          closePipes();
          close(sock);
          break;
      }//switch pid2 bracket
      break;
    }//switch pid1 bracket

return 0;
}
/* This method takes in the unsigned char type which is read in a bitwise manner and translated to a type of Either O or E
It also takes in the size of what it is going to send to a child.
It does not take in the pipes as parameters because these are global variables.

First, it checks to see which child is going to receive the message
Second, it sends the size to the right child
Third, it sends the text to the right child*/
void sendToChildren(unsigned char type, int size, unsigned char* string_to_children){
  int byteswritten;
  if((type >> type_shift) == (int) type_O){//To child 1 (O)
    #ifdef DEBUG
      fprintf(stdout, ">>> %u <<< (P) Sending size to child 1. \n", getpid());
    #endif
    byteswritten = write(pipe1[1], &size, size_int);//sender størrelse
    if(byteswritten == -1){
      fprintf(stdout, ">>> %u <<< (P) Error from writing size to pipe1: %s\n", getpid(), strerror(errno));
    }else{
      #ifdef DEBUG
        fprintf(stdout, ">>> %u <<< (P) Size sent succesfully to child 1. \n", getpid());
      #endif
    }
    #ifdef DEBUG
      fprintf(stdout, ">>> %u <<< (P) Sending text to child 1. \n", getpid());
    #endif
    byteswritten = write(pipe1[1], string_to_children, size);//sender tekst
    if(byteswritten == -1){
      fprintf(stdout, ">>> %u <<< (P) Error from writing text to pipe1: %s\n", getpid(), strerror(errno));
    } else{
      #ifdef DEBUG
        fprintf(stdout, ">>> %u <<< (P) Text sent succesfully to child 1. \n", getpid());
      #endif
    }
  } else if((type >> type_shift) == (int) type_E){//To child 2 (O)
    #ifdef DEBUG
      fprintf(stdout, ">>> %u <<< (P) Sending size to child 2. \n", getpid());
    #endif
    byteswritten = write(pipe2[1], &size, size_int);//sender størrelse
    if(byteswritten == -1){
      fprintf(stdout, ">>> %u <<< (P) Error from writing size to pipe2: %s\n", getpid(), strerror(errno));
    }else{
      #ifdef DEBUG
        fprintf(stdout, ">>> %u <<< (P) Size sent succesfully to child 2. \n", getpid());
      #endif
    }
    #ifdef DEBUG
      fprintf(stdout, ">>> %u <<< (P) Sending text to child 2. \n", getpid());
    #endif
    byteswritten = write(pipe2[1], string_to_children, size);//sender tekst
    if(byteswritten == -1){
      fprintf(stdout, ">>> %u <<< (P) Error from writing text to pipe2: %s\n", getpid(), strerror(errno));
    } else{
      #ifdef DEBUG
        fprintf(stdout, ">>> %u <<< (P) Text sent succesfully to child 2. \n", getpid());
      #endif
    }
  } else{
    #ifdef DEBUG
      fprintf(stdout, ">>> %u <<< (P) Type was not Q or E. Did not send to children\n", getpid());
    #endif
  }
}
/*This method is used to get a number from a user.
It does not ask the user to type a number, so this needs
to be asked for(printed) before calling the method,
so that the user knows that they are supposed to type something*/
int getNumberFromUser(){
  char user_input_buf[255];
  fgets(user_input_buf, 255, stdin);
  int user_input_number = atoi(user_input_buf);
  while(user_input_number == 0){
    fprintf(stdout, "'%s' is not a valid number.\n Type a valid number:", user_input_buf);
    fgets(user_input_buf, 255, stdin);
    user_input_number = atoi(user_input_buf);
  }
  return user_input_number;
}
/*This method prints your options when sending actions
to the server*/
void printMainOptions(){
  fprintf(stdout, "%s\n%s\n%s\n%s\n%s\n",
  "Type one of the following commands:",
  "1 : Ask for 1 new job.",
  "2 : Ask for a chosen amount of jobs.",
  "3 : Ask for all the jobs.",
  "4 : Quit.");
}
/*This method closes the pipes*/
void closePipes(){
  close(pipe1[0]);
  close(pipe1[1]);
  close(pipe2[0]);
  close(pipe2[1]);
}
/*This method sends whatever is in protocol at the time to the server*/
void send_to_Server(){
  size_t sent_bytes = 0;
  size_t sending_size = protocol_size;// protocol_size = 4
  #ifdef DEBUG
    fprintf(stdout, ">>> %u <<< (P) Client writing data to server\n", getpid());
  #endif
  while(((sent_bytes = write(sock, protocol, sizeof(protocol))) > 0) && ((int)sending_size > 0)){
     sending_size -= sent_bytes;
     if(sending_size == 0){
       break;
     }
     #ifdef DEBUG
       fprintf(stdout, ">>> %u <<< (P) Client wrote [%d] bytes. Remaining bytes: [%d].\n", getpid(), (int)sent_bytes, (int)sending_size);
     #endif
  }
  memset(protocol, '\0', sizeof(protocol));
  #ifdef DEBUG
    fprintf(stdout, ">>> %u <<< (P) Sending from Client successful.\n", getpid());
  #endif
}

/*This method is to be run IN THE PARENT PROCESS when ctrl+c is pressed
 *It makes sure that the buffers are freed,
 *then it checks if it has connected to the server, if so it sends a shut down message to the server.
 *then it closes the sockets, closes the pipe, prints a message, and exits*/
void ctrl_c_handler(){
  if(newbuffer != NULL){
    free(newbuffer);
  }
  memset(protocol, '\0', protocol_size);
  protocol[3] |= error;
  if(connected == 1){
    send_to_Server();
  }
  close(sock);
  closePipes();
  fprintf(stdout, "\n>>> %u <<< Killed parent process by ctrl+c.\n", getpid());
  exit(0);
}
/*This method is to be run IN THE CHILD PROCESSES when ctrl+c is pressed
 *It kills them.*/
void ctrl_c_handler_children(){
  fprintf(stdout, "\n>>> %u <<< Killed child  process by ctrl+c.\n", getpid());
  exit(0);
}
/*This method is used to resolve a hostname into an IP adress
 *char* hostname is the hostname to be resolved
 * **ip is a pointer to an integer which will be changed into the
 * hostname's IP
 * returns 0 upon success
 * returns -1 upon error.*/
int getIP(char* hostname, char **ip){
  int errorcode;
  struct addrinfo ai_hints, *ai_servinfo, *ai_serinfo_cpy;
  struct sockaddr_in *sa_h;

  memset(&ai_hints, 0, sizeof(ai_hints));
  ai_hints.ai_family = AF_INET;
  ai_hints.ai_socktype = SOCK_STREAM;

  if((errorcode = getaddrinfo( hostname , "http" , &ai_hints , &ai_servinfo)) != 0){
    fprintf(stderr, "getIP(getaddrinfo): %s\n", gai_strerror(errorcode));
    return -1;
  }
  //loop through all the results and choose the first valid
  for(ai_serinfo_cpy = ai_servinfo; ai_serinfo_cpy != NULL; ai_serinfo_cpy = ai_serinfo_cpy->ai_next){
      sa_h = (struct sockaddr_in *) ai_serinfo_cpy->ai_addr;
      strcpy(*ip , inet_ntoa(sa_h->sin_addr));
  }
  freeaddrinfo(ai_servinfo);
  return 0;
}
