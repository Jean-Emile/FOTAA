#include "flash.h"




void event(int count)
{


            printf("%d\n",count);
	if(count == -38) 
	{
		printf("Successfully flashed\n");
		exit(0);
	}

}



int main(int argc,char **argv[])
{

int lastmem;
   unsigned char file_intel_hex_array[35720];
   int taille =  open_file("/tmp/arduinoGeneratednode0/target/uno/arduinoGeneratednode0.hex",&file_intel_hex_array);
  if(taille <=0)
  {
	printf("file not found\n");
	exit(-1);
 }else {
 	printf(" taille %d \n",taille); 
 }
 

register_FlashEvent(event);

  write_on_the_air_program("/dev/ttyACM0",ATMEGA328,"K000",taille,&file_intel_hex_array[0]);
         sleep(600);


 return 0;
}
