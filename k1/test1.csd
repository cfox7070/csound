<CsoundSynthesizer>
<CsOptions>
-o "test1.wav"
</CsOptions>

<CsInstruments>

sr = 44100
ksmps = 32
nchnls = 2
0dbfs = 1

instr 1
    itail = 0.01
    p3 = p3 + itail
    idur = p3
    iamp = p4
    ifreq = cpspch(p5)

    kenv linseg 0, 0.001, 1, 0.25, 0.4, 2, 0.1, 0, 0.1
    kenvgate linseg, 1, idur - itail, 1, itail, 0, 0, 0
    kenv = kenv * kenvgate

    a1 vco2 1, ifreq * 2, 0 
    a2 vco2 1, ifreq, 2, 0.6 + birnd(0.1)

    ival1 = 7000 + rnd(3000)
    ival2 = 7000 + rnd(3000)
    kenv2 expseg 16000 + rnd(2000), 2, ival1, 4, 2000, 0, 2000
    kenv3 expseg 16000 + rnd(2000), 2, ival2, 4, 2000, 0, 2000

    amix = a2 ;* 0.5 + a2 * 0.5
 ;   amix = amix * kenv * iamp

    afilter1 moogladder amix, kenv2, 0.4 + rnd(0.1)
    afilter2 moogladder amix, kenv3, 0.4 + rnd(0.1)

 ;  outs afilter1, afilter2
    outs amix, amix

    chnmix afilter1, "left"
    chnmix afilter2, "right"
endin
</CsInstruments>

<CsScore>
i1 	0.0000	0.2500	 0.5	 9.02 	   ;d9
i1 	0.2500	0.2500	 0.5	 9.04 	   ;e9
i1 	0.5000	0.2500	 0.5	 9.05 	   ;f9
i1 	0.7500	0.2500	 0.5	 9.07 	   ;g9
i1 	1.0000	0.2500	 0.5	 9.09 	   ;a9
i1 	1.2500	0.2500	 0.5	 8.09 	   ;a8
i1 	1.5000	0.2500	 0.5	 9.01 	   ;c#9
i1 	1.7500	0.2500	 0.5	 8.09 	   ;a8
i1 	2.0000	1.5000	 0.5	 9.02 	   ;d9

</CsScore>
</CsoundSynthesizer>
<bsbPanel>
 <label>Widgets</label>
 <objectName/>
 <x>100</x>
 <y>100</y>
 <width>320</width>
 <height>240</height>
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
