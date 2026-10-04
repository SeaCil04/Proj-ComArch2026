#ifndef EXECUTOR_H
#define EXECUTOR_H

#include "../Instruction.h"
#include "../stateStruct/stateStruct.h"

/* instruction executor class */
class Executor {
public:
    /* execute instruction: returns 1 to continue, 0 to halt */
    bool execute(const Instruction& instruction, stateStruct& state);
};

#endif