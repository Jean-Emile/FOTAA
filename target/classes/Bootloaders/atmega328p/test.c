#define MAX_SIZE_ID 5
const char ID[MAX_SIZE_ID]= "K000";

void putchKID()
{
    uint8_t i=0;
	for(i=0;i< MAX_SIZE_ID;i++){
	 printf("%c",ID[i]);	
	}
}



int main(void)
{
putchKID();
}
