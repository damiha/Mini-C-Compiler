
// this is a C program to compute n! (n factorial)

int main(){
    print("5! is")
    print(factorial(5));
}

int factorial(int n){
    if(n <= 1){
        return 1;
    }
    return n * factorial(n - 1);
}