

int main(){

    // pointers on ints
    int x = 5;
    int* xp = &x;

    print(x); // 5
    *xp = 10;
    print(x); // 10

    // pointers into arrays
    int arr[5];
    int* ap = &arr[3];
    arr[3] = 8;
    print(arr[3]); // 8
    *ap = 18;
    print(arr[3]); // 18

    // array of pointers
    int* parr[2];
    parr[0] = xp;
    parr[1] = ap;

    print(*parr[0]); // 10
    print(*parr[1]); // 18

    *parr[0] = 25;
    print(x); // 25

    // int** p2p = &ap; // pointers to pointers (doesn't work yet)
}