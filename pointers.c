
int y = 5;
int* yp = &y;
int arr[5];

int main(){
    int x = 5;
    int* xp = &x;

    *xp = 10;

    print(x);
}