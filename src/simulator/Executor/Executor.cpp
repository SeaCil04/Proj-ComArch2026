#include "Executor.h"

/* execute a single instruction and update machine state */
bool Executor::execute(const Instruction& instruction, stateStruct& state) {
    switch (instruction.opcode) {
        case 0: /* add */
            state.registers[instruction.destReg] = state.registers[instruction.regA] + state.registers[instruction.regB];
            state.pc++;
            break;

        case 1: /* nand */
            state.registers[instruction.destReg] = ~(state.registers[instruction.regA] & state.registers[instruction.regB]);
            state.pc++;
            break;

        case 2: /* lw */
            {
                int address = state.registers[instruction.regA] + instruction.offset;

                /* check memory bounds */
                if (address < 0 || address >= NUMMEMORY) {
                    return false; 
                }

                state.registers[instruction.regB] = state.memory[address];
                state.pc++;
                break;
            }

        case 3: /* sw */
            {
                int address = state.registers[instruction.regA] + instruction.offset;

                /* check memory bounds */
                if (address < 0 || address >= NUMMEMORY) {
                    return false; 
                }

                state.memory[address] = state.registers[instruction.regB];
                state.pc++;
                break;
            }

        case 4: /* beq */
            if (state.registers[instruction.regA] == state.registers[instruction.regB]) {
                state.pc += instruction.offset + 1;
            } else {
                state.pc++;
            }
            break;

        case 5: /* jalr */
            {
                int nextPC = state.registers[instruction.regA];

                state.registers[instruction.regB] = state.pc + 1;
                state.pc = nextPC;
                break;
            }

        case 6: /* halt */
            state.pc++;
            return false;

        case 7: /* noop */
            state.pc++;
            break;
            
        default:
            /* invalid opcode */
            return false;
    }

    /* register 0 is hardwired to 0 */
    state.registers[0] = 0;

    return true;
}