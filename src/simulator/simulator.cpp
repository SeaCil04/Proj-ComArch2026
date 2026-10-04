#include <iostream>
#include <cstdio>
#include "stateStruct/stateStruct.h"
#include "Loader/Loader.h"
#include "Decoder/Decoder.h"
#include "Executor/Executor.h"

using namespace std;

void printState(stateStruct *state);

int main(int argc, char* argv[])
{
    stateStruct state;
    Loader loader;
    Decoder decoder;
    Executor executor;
    int totalInstructions = 0;

    if (argc != 2) {
        printf("error: usage: %s <machine-code file>\n", argv[0]);
        return 1;
    }

    /* load machine code into memory */
    loader.loadProgram(argv[1], state);
    
    /* fetch-decode-execute loop */
    while (true) {
        /* print state before executing instruction */
        printState(&state);

        /* fetch */
        int machineCode = state.memory[state.pc];

        /* decode */
        Instruction instruction = decoder.decode(machineCode);
        totalInstructions++;

        /* execute */
        bool continueEx = executor.execute(instruction, state);

        if (!continueEx) {
            printf("machine halted\ntotal of %d instructions executed\nfinal state of machine:\n", totalInstructions);
            break;
        }
    }

    /* print state once after halt */
    printState(&state);

    return 0;
}

/* print current state of the machine */
void printState(stateStruct *statePtr)
{
    int i;
    printf("\n@@@\nstate:\n");
    printf("\tpc %d\n", statePtr->pc);
    printf("\tmemory:\n");
    for (i = 0; i < statePtr->numMemory; i++) {
        printf("\t\tmem[ %d ] %d\n", i, statePtr->memory[i]);
    }
    printf("\tregisters:\n");
    for (i = 0; i < NUMREGS; i++) {
        printf("\t\treg[ %d ] %d\n", i, statePtr->registers[i]);
    }
    printf("end state\n");
}