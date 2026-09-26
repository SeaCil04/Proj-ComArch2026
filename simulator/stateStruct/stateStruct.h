#ifndef stateStruct_H
#define stateStruct_H

class stateStruct {
public:
    int pc;
    int registers[8];
    int memory[65536];
    int numMemory;

    stateStruct();
};

#endif  