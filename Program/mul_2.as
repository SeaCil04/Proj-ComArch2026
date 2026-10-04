        lw      0       2       mcand   r2 = multiplicand (shifted left each round)
        lw      0       3       mplier  r3 = multiplier
        lw      0       4       one     r4 = bit mask, starts at 1
        lw      0       5       cnt     r5 = loop counter (15 bits)
        lw      0       7       neg1    r7 = -1
loop    nand    3       4       6       r6 = ~(mplier & mask)
        nand    6       6       6       r6 = mplier & mask
        beq     6       0       skip    bit is 0 -> do not add
        add     1       2       1       result += multiplicand
skip    add     2       2       2       multiplicand <<= 1
        add     4       4       4       mask <<= 1
        add     5       7       5       counter--
        beq     5       0       done    finished 15 bits
        beq     0       0       loop
done    halt                            result in reg1
mcand   .fill   32766
mplier  .fill   10383
one     .fill   1
cnt     .fill   15
neg1    .fill   -1
