#ifndef DECODER_H
#define DECODER_H

#include "Instruction.h"

class Decoder {
public:
    Instruction decode(int machineCode);

private:
    int convertNum(int num);
};

#endif