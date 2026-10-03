#ifndef stateStruct_H
#define stateStruct_H

#define NUMMEMORY 65536 /* maximum number of words in memory */
#define NUMREGS 8 /* number of machine registers */
#define MAXLINELENGTH 1000

class stateStruct {
public:
    int pc;
    int registers[NUMREGS];
    int memory[NUMMEMORY];
    int numMemory;

    stateStruct();
};

#endif  