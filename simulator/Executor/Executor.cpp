#include "Executor.h"

bool Executor::execute(const Instruction& instruction, stateStruct& state) {
    switch (instruction.opcode) {
        case 0: // add
            state.registers[instruction.destReg] = state.registers[instruction.regA] + state.registers[instruction.regB];
            state.pc++;
            break;
        case 1: // nand
            state.registers[instruction.destReg] = ~(state.registers[instruction.regA] & state.registers[instruction.regB]);
            state.pc++;
            break;
        case 2: // lw
            state.registers[instruction.regB] = state.memory[state.registers[instruction.regA] + instruction.offset];
            state.pc++;
            break;
        case 3: // sw
            state.memory[state.registers[instruction.regA] + instruction.offset] = state.registers[instruction.regB];
            state.pc++;
            break;
        case 4: // beq
            if (state.registers[instruction.regA] == state.registers[instruction.regB]) {
                state.pc += instruction.offset + 1;
            } else {
                state.pc++;
            }
            break;
        case 5: // jalr
            {
                int nextPC = state.registers[instruction.regA];

                state.registers[instruction.regB] = state.pc + 1;
                state.pc = nextPC;

                break;
            }
        case 6: // halt
            state.pc++;
            return false;
        case 7: // noop
            state.pc++;
            break;
            
        default:
            // Invalid opcode
            return false;
    }
    return true;
}