        lw      0       1       a       r1 = a
        lw      0       2       b       r2 = b
        lw      0       4       one     r4 = 1
        nand    2       2       2       r2 = ~b
        add     2       4       2       r2 = ~b + 1 = -b (two's complement)
        add     1       2       3       r3 = a - b
        halt                            answer in reg3
a       .fill   100
b       .fill   37
one     .fill   1
