
int isPrime(int n){
    if(n <= 1){
        return 0;
    }

    int i = 2;

    while(i < n){

        if(n % i == 0){
            return 0;
        }
        i = i + 1;
    }
    return 1;
}

int main(){

    int i = 2;
    int max = 100;

    while(i < max){

        if(isPrime(i)){
            print(i);
        }
        i = i + 1;
    }
}