
# Mini C Compiler And Virtual Machine

This is my first attempt at building a minimalistic compiler for the C language. The C code gets translated into VM code (the VM has a custom instruction set).
This repo contains:
  1. A lexer that chunks the plain source text (a string) into tokens like <IDENTIFIER, "n">
  2. A parser that builds an abstract syntax tree from the token stream
  3. A code generator (compiler) that generates VM instructions
  4. A virtual machine (VM) with a small (although Turing-complete) instruction set


## C Code to VM instructions

The translation from a .c into a .cma (C-Machine file) is done by running the "CompileCode" main method.

Input (to show that recursion works):
```
int main(){
    print("5! is");
    print(factorial(5));
}

int factorial(int n){
    if(n <= 1){
        return 1;
    }
    return n * factorial(n - 1);
}
```

Output:
```
LoadC 0
Mark
LoadC 'main'
Call
Slide 0
Halt
main: LoadC '5! is'
Print
LoadC 5
LoadC 0
Mark
LoadC 'factorial'
Call
Slide 1
Print
LoadC 0
LoadRC -3
Store
Return
factorial: LoadRC -4
Load
LoadC 1
LessOrEqual
JumpZ 0
LoadC 1
LoadRC -3
Store
Return
0: LoadRC -4
Load
LoadRC -4
Load
LoadC 1
Sub
LoadC 0
Mark
LoadC 'factorial'
Call
Slide 1
Mul
LoadRC -3
Store
Return
LoadC 0
LoadRC -3
Store
Return
```

## Running the VM code

Executing the VM code is done by calling the "RunCode" main method
Output:
```
VM: 5! is
VM: 120
VM: exited with code 0
```

## Some more examples

Printing the primes up to 100:
```
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
```

Computing the 20th Fibonacci number (F_19) using arrays:
```
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
```

Working with pointers (pointer to pointer doesn't work yet):
```
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
}
```

The repo also contains the "operators.c" file which demonstrates that all standard unary and binary operations work as expected.

## What's missing?

- Structs
- Other data types apart from int and int*
- A type checker
- Dynamic memory allocation with  malloc and free
- Garbage collection (inside the VM)
- Short-circuiting of && and || expressions
- Pointer to pointer like "int** p2p;"
- Array initialization like "int arr[] = {2, 3, 5};"
- break and continue
- for loops and do-while loops (only while loops are available and I didn't want to translate for-loops into while loops in the AST because they behave differently with respect to "continue")
- syntactic sugar like "i++;"

  ## Notes

The book [Crafting Interpreters](https://craftinginterpreters.com/) is amazing and taught me how to right the lexer and the recursive descent parser.
I have taken the context-free grammar from there (with some modifications). I've read the first half of the book (the tree-walker interpreter) and learned how to write the VM and code generation parts using university slides.
