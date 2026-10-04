#ifndef EXECUTOR_H
#define EXECUTOR_H

#include "../Instruction.h"
#include "../stateStruct/stateStruct.h"

class Executor {

public:

    bool execute(const Instruction& instruction, stateStruct& state);
};

#endif