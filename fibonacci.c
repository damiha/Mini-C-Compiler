
int main(){

    int arr[20];
    arr[0] = 0;
    arr[1] = 1;

    int i = 2;
    while(i < 20){
        arr[i] = arr[i - 1] + arr[i - 2];
        i = i + 1;
    }

    print(arr[19]);
}