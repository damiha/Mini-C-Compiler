
int main(){

    print("unary minus and negation");
    print(-3 < 4); // 1 (=True)
    print(-3 < -3); // 0 (=False)

    print(!(-3 < -3)); // 1 (=True)

    print("arithmetic");
    print(100 + 25); // 125
    print(100 - 25); // 75
    print(100 * 25); // 2500
    print(100 / 25); // 4
    print(100 % 25); // 0

    print("or");
    print(0 || 10); // 1 (every value except 0 is treated as True)
    print(0 || -1); // 1
    print(1 || -1); // 1
    print(0 || 0); // 0

    print("and");
    print(0 && 0); // 0
    print(1 && 0); // 0
    print(0 && 1); // 0
    print(-1 && 1); // 1

    print("ordering relations");
    print(3 < 5);
    print(3 < 3);

    print(3 <= 3);
    print(3 <= 2);

    print(4 == 4);
    print(4 == 5);

    print(5 > 4);
    print(4 > 4);

    print(5 >= 5);
    print(5 >= 10);
}