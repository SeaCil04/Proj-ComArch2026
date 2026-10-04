        lw      0       1       n       r1 = n
        lw      0       2       r       r2 = r
        lw      0       6       cAdr    r6 = address of comb
        jalr    6       7               call comb(n,r); r7 = return address
        halt                            answer in reg3
comb    sw      5       7       stack   push return address
        lw      0       6       pos1
        add     5       6       5       sp++
        sw      5       1       stack   push n
        add     5       6       5       sp++
        sw      5       2       stack   push r
        add     5       6       5       sp++
        beq     2       0       base    r == 0 -> return 1
        beq     1       2       base    n == r -> return 1
        lw      0       6       neg1
        add     1       6       1       n = n-1
        lw      0       6       cAdr
        jalr    6       7               comb(n-1, r) -> r3
        sw      5       3       stack   push result of first call
        lw      0       6       pos1
        add     5       6       5       sp++
        lw      0       6       neg1
        add     2       6       2       r = r-1
        lw      0       6       cAdr
        jalr    6       7               comb(n-1, r-1) -> r3
        lw      0       6       neg1
        add     5       6       5       sp--
        lw      5       4       stack   r4 = first result
        add     3       4       3       r3 = sum
        beq     0       0       ret
base    lw      0       3       pos1    r3 = 1
ret     lw      0       6       neg1
        add     5       6       5       sp--
        lw      5       2       stack   restore r
        add     5       6       5       sp--
        lw      5       1       stack   restore n
        add     5       6       5       sp--
        lw      5       7       stack   restore return address
        jalr    7       6               return
n       .fill   7
r       .fill   3
pos1    .fill   1
neg1    .fill   -1
cAdr    .fill   comb
stack   .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
        .fill   0
