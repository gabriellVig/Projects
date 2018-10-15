#include <stdio.h>
#include <stdlib.h>

#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <signal.h>

#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>



//Sizes
#define size_char  1
#define size_int 4
#define size_int_char 5

//Defines for job bits
#define job_1 1
#define job_1_shift 0
#define job_n 2
#define job_n_shift 1
#define job_all 4
#define job_all_shift 2
#define quit 8
#define quit_shift 3
#define error 16
#define error_shift 4

//defines for type
#define type_shift 5
#define type_O 0
#define type_E 1
#define type_QUIT 7

#define protocol_size 4//protocol size

//Method declarations. Method explanations and content found after main.
unsigned char* readfile(unsigned int* size, unsigned char* type);
void read_n_jobs(unsigned char* buf,  unsigned char* buf_1_job,  unsigned char* buf_n_jobs,  unsigned int* size,  unsigned char* type, int read_all_jobs);
void send_to_Client(unsigned char* send_this, int sending_size);
void print_byte(uint8_t byte);
void ctrl_c_handler();


const char *bit_rep[16] = { //For testing purposes /Printing binary
  [ 0] = "0000", [ 1] = "0001", [ 2] = "0010", [ 3] = "0011",
  [ 4] = "0100", [ 5] = "0101", [ 6] = "0110", [ 7] = "0111",
  [ 8] = "1000", [ 9] = "1001", [10] = "1010", [11] = "1011",
  [12] = "1100", [13] = "1101", [14] = "1110", [15] = "1111",
};
//global variables
FILE * filen;
int sock;

int main(int argn, char* argv[]){
  signal(SIGINT, ctrl_c_handler);//Signal for ctrl+
  //ARGV TEST START
  if(argn != 3){
    printf("ERROR: Correct usage: ./program_name <job_file> <port_number(1025...n)>\n");
    exit(0);
  }
  int portnumber = atoi(argv[2]);
  if( portnumber < 1024){
    printf("ERROR: portnumber < 1024.\nExiting.\n");
    exit(0);
  }

  filen = fopen(argv[1], "rb");//åpner filen
  if(filen == NULL){
    printf("Error from fopen(): %s\nExiting.\n", strerror(errno));
    exit(0);
  }
  //ARGV TEST END

//Server startup START #
  struct sockaddr_in serveraddr, clientaddr;
  socklen_t clientaddrlen;
  int request_sock;
  unsigned char buf[protocol_size]; //For protokollene

  request_sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);// Opprett request-socket
  if(request_sock == -1){
    fclose(filen);
    fprintf(stdout, "Error from socket(): %s\nExiting.\n", strerror(errno));
    exit(0);
  }

  memset(&serveraddr, 0, sizeof(serveraddr));
  serveraddr.sin_family = AF_INET;
  serveraddr.sin_addr.s_addr = INADDR_ANY;
  serveraddr.sin_port = htons(portnumber);
  clientaddrlen = sizeof(clientaddr);
  bind(request_sock, (struct sockaddr *)&serveraddr, sizeof(serveraddr));// Bind adressen til socketen
  listen(request_sock, SOMAXCONN);//Aktiver lytting p� socketen
  sock = accept(request_sock, (struct sockaddr *)&clientaddr, &clientaddrlen);//Motta en forbindelse
  close(request_sock);
//SERVER startup END %

  //Declarations
  unsigned char type;
  unsigned int size;
  unsigned char* buf_1_job;
  unsigned char* buf_n_jobs = NULL;//for when I'm sending more than 1 job with 1 write to socket

  while(0 < 1){//Forever
//READING FROM SOCKET START #
    #ifdef DEBUG
      fprintf(stdout, "Reading from socket.\n");
    #endif
    int readsize = read(sock, buf, protocol_size);//Reading what the client wants
    if(readsize != protocol_size){
      fprintf(stdout, "Error from reading the socket. Error: %s", strerror(errno));
    } else{
      #ifdef DEBUG
        fprintf(stdout, "Read from socket succesfully. Type in binary:");
        print_byte(buf[3]);
      #endif
    }
//READING FROM SOCKET END  %

    //IF and else if tests for what the client wants
    if((buf[3] >> job_1_shift) == 1){//Sending 1 job
      buf_1_job = readfile(&size, &type);
      if(buf_1_job == NULL){//If there isn't more to read
        #ifdef DEBUG
          fprintf(stdout ,"Nothing more to read. Readfile returned NULL.\nSending quit message to Client\n");
        #endif
        unsigned char sendtoTurnOff[size_int_char];
        memset(sendtoTurnOff, '\0', size_int_char);
        sendtoTurnOff[0] |= type_QUIT << type_shift;
        send_to_Client(sendtoTurnOff, size_int_char);
      } else{
        send_to_Client(buf_1_job, size + size_int_char);
        free(buf_1_job);
      }
    }
    else if((buf[3] >> job_n_shift) == 1){ //Sending n jobs
      read_n_jobs(buf, buf_1_job, buf_n_jobs, &size, &type, 0);
    }
    else if((buf[3] >> job_all_shift) == 1){//Sending all jobs
      read_n_jobs(buf, buf_1_job, buf_n_jobs, &size, &type, 1);
    }
    else if((buf[3] >> quit_shift) == 1){//quitting
      #ifdef DEBUG
        fprintf(stdout, "Received quit message from Client.\n");
      #endif
      break;
    } else if((buf[3] >> error_shift) == 1){//Error received from client
      #ifdef DEBUG
      fprintf(stdout, "Received error message from Client.\n");
      #endif
      break;

    }
  }

  unsigned char sendtoTurnOff[size_int_char];//to be sent to client
  memset(sendtoTurnOff, '\0', size_int_char);
  sendtoTurnOff[0] |= type_QUIT << type_shift;//making it the type to make client shut down
  send_to_Client(sendtoTurnOff, size_int_char);//Sending it
  fprintf(stdout ,"Server quitting.\n");
  fclose(filen);
  close(request_sock);
  close(sock);
  return 0;
}

/*This method takes in two pointers, one for the size and one for the type
 *These are set by the method according to what it is reading from the file
 *This method does not take in the file as a parameter because it is already global

 *It returns NULL if there is nothing more to be read.
 *Otherwise, it returns an UNFREED char* which contains a job text,
 *REMEMBER TO FREE THE RETURN POINTER AFTER USE*/
unsigned char* readfile(unsigned int* size, unsigned char* type){
  if(feof(filen) != 0){
    return NULL;
  }
  int freadOkay;

  freadOkay = fread(type, size_char, 1, filen);//reading 1 char -> jobtype
  if(freadOkay != 1){
    if(feof(filen)){
      #ifdef DEBUG
        fprintf(stdout, "Reached end of file. Readfile returning NULL\n");
      #endif
      return NULL;
    } else if(ferror(filen)){
      #ifdef DEBUG
        fprintf(stdout, "Error from file. Readfile Returning NULL");
      #endif
      return NULL;
    }
    #ifdef DEBUG
      fprintf(stdout, "Fread did not read enough objects(Job type)\n");
    #endif

    return NULL;
  }

  freadOkay = fread(size, size_int, 1, filen);//reading 1 int -> text length
  if(freadOkay != 1){
    #ifdef DEBUG
      fprintf(stdout, "Fread did not read enough objects (Job length)\n");
    #endif
    return NULL;
  }

  unsigned char* returnbuff = malloc(size_int_char + *size);//Allocating memory. THIS IS NOT FREED INSIDE THE METHOD

  returnbuff[0] = 0;//Zero's out all the bits for type
  returnbuff[1] = (*size >> 24) & 0xFF;//bitshift to get the int sent properly
  returnbuff[2] = (*size >> 16) & 0xFF;
  returnbuff[3] = (*size >> 8) & 0xFF;
  returnbuff[4] = *size & 0xFF;

  unsigned char* buff = malloc(*size + 1);//Allocating memory for the job text This gets freed inside the method
  buff[*size] = '\0';
  //I need to have two buffers because i put the buffer with job text inside the returnbuffer TOGETHER with the type and size and checksum.

  freadOkay = fread(buff, size_char, *size, filen);//Reading job text
  if(freadOkay !=(int) *size){
    #ifdef DEBUG
      fprintf(stdout, "Fread did not read enough objects(Job text)\n");
    #endif
    free(returnbuff);
    free(buff);
    return NULL;
  }
  //Placing jobtext inside returnbuff and calculating checksum
  int index, bufindex, checksum;
  for(index = 5, bufindex = 0, checksum = 0; index < (int)(*size + 5) && bufindex < (int)*size; index++, bufindex++){
    returnbuff[index] = buff[bufindex];//putting text into the message being sent
    checksum += buff[bufindex];//Adding all char values
  }

  checksum %= 32;//Checksum calculated
  returnbuff[0] = checksum;//Putting checksum inside returnbuff
  if(*type == 'O'){//Checking what type it is
    returnbuff[0] |= type_O << type_shift;//Bitmask according to type
    #ifdef DEBUG
      fprintf(stdout,"Type: O Size: %u Checksum: %u.\n",*size, checksum);
    #endif
  } else if(*type == 'E'){
    returnbuff[0] |= type_E << type_shift;
    #ifdef DEBUG
    fprintf(stdout,"Type: E Size: %u Checksum: %u.\n",*size, checksum);
    #endif
  }
  free(buff);//Freeing the temporary buff which was used to house the job text
  return returnbuff;//RETURNING UNFREED char*
}

/*This method takes in a pointer to the protocol (buf)
  unsigned char* buf:         a pointer to the buffer for 1 job
  unsigned char* buf_1_job:   a pointer to the buffer for n_jobs
  unsigned char* buf_n_jobs:  a pointer to what kind of type a job is(So that it can be used after excecuting the method)
  unsigned int* size:         a pointer to the size of a job (So that it can be us ed after excecuting the method)
  int read_all_jobs:          when set to 0, read_n_jobs sends the requested amount of jobs,
                              when set to 1, read_n_jobs sends all the jobs.

  Firstly, it tries to read one job. If this is unsuccesful, then it is set to NULL and skips the first if test and while loop.
  then it sends a quit message to the client.
  If it is successful and the client asked for more then 1 job, the while loop is initiated
  inside the while loop, buf_1_job calls readfile. If the return value == NULL, it sends what it already has read and thereafter sends a quit message to client.
                                                   If the return value != NULL, it reallocates space for buf_n_jobs, and puts the new job inside(after the previus job)
  Continuing onwards, if the method hasn't yet sent buf_n_jobs, then it does now.
  It then makes sure to free buf_n_jobs
*/
void read_n_jobs(unsigned char* buf, unsigned char* buf_1_job, unsigned char* buf_n_jobs, unsigned int* size, unsigned char* type, int read_all_jobs){
  int totalsize, amount_jobs_from_client, count_jobs_read = 0, sent_current_task = 0;//Int Declarations
  amount_jobs_from_client = (buf[0] << 16) + (buf[1] << 8) + (buf[2]);//bitshifting back (3 bytes to translate into a int)
  buf_1_job = readfile(size, type);//Tries to read 1 job
  #ifdef DEBUG
  if(read_all_jobs == 1){
    fprintf(stdout, "Sending ALL jobs to Client.\n");
  } else{
    fprintf(stdout, "Trying to send %u jobs to Client.\n", amount_jobs_from_client);
  }
  #endif

  if(buf_1_job != NULL){//If it managed to read 1 job
    totalsize = *size + size_int_char;//The total size is set to the first jobs size
    buf_n_jobs = (unsigned char*) malloc(totalsize);//Allocating memory for the buffer which will contain n OR all jobs
    memcpy(buf_n_jobs, buf_1_job, totalsize);//Since buf_n_jobs is empty, I use memcpy to put everything from buf_1_job into buf_n_jobs
    free(buf_1_job);//free buf_1_job
    count_jobs_read++;
  }

  //while there is more to read and n jobs have not been read.
  while(buf_1_job != NULL && (count_jobs_read < amount_jobs_from_client || read_all_jobs == 1)){
    buf_1_job = readfile(size, type);//getting new job
    if(buf_1_job != NULL){//if the job from readfile is valid
      totalsize += *size + size_int_char;//Increasing total size
      buf_n_jobs = realloc(buf_n_jobs, totalsize);//Reallocating space for buf_n_jobs: realloc buf_n_jobs
      int index;//index for this inner loop
      //Loop to place contents of buf_1_job inside buf_n_jobs correctly
      for(index = 0; index < (int)(*size + size_int_char); index++){ //(int)(*size + size_int_char) is the size of: job text + type + size
        buf_n_jobs[index + totalsize - *size - size_int_char] = buf_1_job[index];
        //totalsize - *size - size_int_char= where the new job is placed from, + index to set following bytes(chars)
      }
      free(buf_1_job);//freeing
      count_jobs_read++;
    } else{//If buf_1_job == NULL then it sends what it already has read
      send_to_Client(buf_n_jobs, totalsize);
      sent_current_task = 1;//remembers that it has sent the job
    }
  }
  if(buf_1_job == NULL){//If there isn't more to be read, the method asks client to turn off.
    #ifdef DEBUG
      fprintf(stdout, "Sending %u jobs to Client\n", count_jobs_read);
      fprintf(stdout, "Nothing more to read. readfile returned NULL.\nSending quit message to Client\n");
    #endif
    unsigned char sendtoTurnOff[size_int_char];
    memset(sendtoTurnOff, '\0', size_int_char);
    sendtoTurnOff[0] |= type_QUIT << type_shift;
    send_to_Client(sendtoTurnOff, size_int_char);
  } else{//Else, it checks if it already has sent a job, if not, it sends the jobs over.
    if(sent_current_task == 0){
      fprintf(stdout, "Sent %u jobs to Client\n", count_jobs_read);
      send_to_Client(buf_n_jobs, totalsize);
    }
  }
  free(buf_n_jobs);//Free
}
/*This method takes in a char* and sends it to the client with a size of sending_size*/
void send_to_Client(unsigned char* send_this, int sending_size){
  size_t sent_bytes = 0;
  #ifdef DEBUG
    fprintf(stdout ,"Writing data to client\n");
  #endif
  while(((sent_bytes = write(sock, send_this, sending_size)) > 0) && (sending_size > 0)){
     sending_size -= sent_bytes;
     if(sending_size == 0){
       break;
     }
     #ifdef DEBUG
       fprintf(stdout, "Server wrote [%d] bytes. Remaining bytes: [%d].\n", (int)sent_bytes, sending_size);
     #endif
  }
  #ifdef DEBUG
    fprintf(stdout, "Sending from Server successful.\n");
  #endif
}
//This method prints a byte in binary.
void print_byte(uint8_t byte){
    printf("%d -> %s%s\n",byte, bit_rep[byte >> 4], bit_rep[byte & 0x0F]);
}
/*This method is called when ctrl+c is clicked.
It sends a quit message to client.
It also closes the file, the socket and then exits*/
void ctrl_c_handler(){
  fprintf(stdout, "ctrl + c clicked.\n");
  unsigned char sendtoTurnOff[size_int_char];
  memset(sendtoTurnOff, '\0', size_int_char);
  sendtoTurnOff[0] |= type_QUIT << type_shift;
  #ifdef DEBUG
    fprintf(stdout, "Sending quit message to Client.\n");
  #endif
  send_to_Client(sendtoTurnOff, size_int_char);
  //Making client quit END

  fclose(filen);
  close(sock);
  fprintf(stdout, "Exiting.\n");
  exit(0);
}
