#ifndef STATESTRUCT_H
#define STATESTRUCT_H

#define NUMMEMORY 65536 /* maximum number of words in memory */
#define NUMREGS 8       /* number of machine registers */
#define MAXLINELENGTH 1000

/* system state structure */
class stateStruct {
public:
    int pc;                   /* program counter */
    int registers[NUMREGS];   /* general-purpose registers */
    int memory[NUMMEMORY];    /* system memory */
    int numMemory;            /* number of words loaded in memory */

    stateStruct();            /* initialize hardware state */
};

#endif