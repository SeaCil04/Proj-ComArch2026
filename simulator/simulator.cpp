#include <iostream>

#include "stateStruct.h"
#include "Loader.h"
#include "Decoder.h"
#include "Executor.h"

using namespace std;

void printState(stateStruct *state);

int main(int argc, char* argv[])
{
    if (argc != 2) {
        cerr << "Usage: simulator <machine-code file>" << endl;
        return 1;
    }


    stateStruct state;
    Loader loader;
    Decoder decoder;
    Executor executor;
    int totalInstructions = 0;

    loader.loadProgram(argv[1], state);
    
    while (true) {
        printState(&state);
        int machineCode = state.memory[state.pc];
        Instruction instruction = decoder.decode(machineCode);
        totalInstructions++;
        bool continueEx = executor.execute(instruction, state);

        if (!continueEx) {
            printf("machine halted\ntotal of %d instructions executed\nfinal state of machine:\n", totalInstructions);
            break;
        }
    }
    printState(&state);
    return 0;
}

void printState(stateStruct *statePtr)
{
    int i;
    printf("\n@@@\nstate:\n");
    printf("\tpc %d\n", statePtr->pc);
    printf("\tmemory:\n");
	for (i=0; i<statePtr->numMemory; i++) {
	    printf("\t\tmem[ %d ] %d\n", i, statePtr->memory[i]);
	}
    printf("\tregisters:\n");
	for (i=0; i<8; i++) {
	    printf("\t\treg[ %d ] %d\n", i, statePtr->registers[i]);
	}
    printf("end state\n");
}