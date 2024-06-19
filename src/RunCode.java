public class RunCode {

    public static void main(String[] args) {

        String fileName = "sample_program.cma";

        Code code = new Code(fileName);

        VirtualMachine vm = new VirtualMachine();

        vm.execute(code);
    }
}
