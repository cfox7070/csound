<CsoundSynthesizer>
<CsOptions>
-o "gammaHarp.wav"
</CsOptions>
<CsInstruments>
sr = 44100
kr = 44100
ksmps = 1
nchnls = 2
0dbfs = 1.0

instr 1
    itail = 0.01
    p3 = p3 + itail
    idur = p3 
    iamp = p4
    ifreq = cpspch(p5)

    kenv linseg 0, 0.001, 1, 0.25, 0.4, 2, 0.1, 0, 0.1
    kenvgate linseg, 1, idur - itail, 1, itail, 0, 0, 0
    kenv = kenv * kenvgate

    if (ifreq < 220) then
        a1 vco2 1, ifreq * 4, 0 
        a2 vco2 1, ifreq * 2, 0 
        ;a2 vco2 1, ifreq, 2, 0.6 + birnd(0.1)
        a3 vco2 1, ifreq, 4, 0.6 + birnd(0.1)
        amix = a1 * 0.25 + a2 * 0.25 + a3 * 0.6   
    else
        a1 vco2 1, ifreq * 2, 0 
        ;a2 vco2 1, ifreq, 2, 0.6 + birnd(0.1)
        a2 vco2 1, ifreq, 4, 0.6 + birnd(0.1)
        amix = a1 * 0.5 + a2 * 0.5
    endif

    ival1 = 7000 + rnd(3000)
    ival2 = 7000 + rnd(3000)
    kenv2 expseg 16000 + rnd(2000), 2, ival1, 4, 2000, 0, 2000
    kenv3 expseg 16000 + rnd(2000), 2, ival2, 4, 2000, 0, 2000

    
    amix = amix * kenv * iamp

    afilter1 moogladder amix, kenv2, 0.4 + rnd(0.1)
    afilter2 moogladder amix, kenv3, 0.4 + rnd(0.1)

   outs afilter1, afilter2
 ;   outs amix, amix

    chnmix afilter1, "left"
    chnmix afilter2, "right"
endin

instr 2
    iamp = p4
    idelay_left = p5
    idelay_right = p6
    iroom_size = p7
    iHFDamp = p8

    a1 chnget "left"
    a2 chnget "right"

    a1 delay a1, idelay_left
    a2 delay a2, idelay_right
    
    denorm a1,a2 ;!!

    a1, a2 freeverb a2, a1, iroom_size, iHFDamp
    outs a1 * iamp, a2 * iamp

    chnclear "left"
    chnclear "right"
endin

</CsInstruments>
<CsScore>

;i2 0 90 2.333 0.0223 0.0213 0.4 0.3

;t 0 75
s
i2 0 55 1.333 0.0223 0.0213 0.4 0.97


#include "gamma.sco"
  
e
</CsScore>
</CsoundSynthesizer>
<bsbPanel>
 <label>Widgets</label>
 <objectName/>
 <x>0</x>
 <y>0</y>
 <width>0</width>
 <height>0</height>
 <visible>true</visible>
 <uuid/>
 <bgcolor mode="nobackground">
  <r>255</r>
  <g>255</g>
  <b>255</b>
 </bgcolor>
</bsbPanel>
<bsbPresets>
</bsbPresets>
