<CsoundSynthesizer>
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

    a1 vco2 1, ifreq * 2, 0 
    a2 vco2 1, ifreq, 2, 0.6 + birnd(0.1)

    ival1 = 7000 + rnd(3000)
    ival2 = 7000 + rnd(3000)
    kenv2 expseg 16000 + rnd(2000), 2, ival1, 4, 2000, 0, 2000
    kenv3 expseg 16000 + rnd(2000), 2, ival2, 4, 2000, 0, 2000

    amix = a1 * 0.4 + a2 + 0.6
    amix = amix * kenv * iamp

    afilter1 moogladder amix, kenv2, 0.4 + rnd(0.1)
    afilter2 moogladder amix, kenv3, 0.4 + rnd(0.1)

    outs afilter1, afilter2

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

    a1, a2 freeverb a2, a1, iroom_size, iHFDamp
    outs a1 * iamp, a2 * iamp

    chnclear "left"
    chnclear "right"
endin

</CsInstruments>
<CsScore>

;i2 0 90 2.333 0.0223 0.0213 0.4 0.3

;Measure4 1     first mesure
;top
i 1 	0.2500	0.2500	 0.5 	 8.00 	  	;c
i 1 	0.5000	0.2500	 0.5 	 8.02 	  	;d
i 1 	0.7500	0.2500	 0.5 	 8.04 	  	;e
i 1 	1.0000	0.2500	 0.5 	 8.05 	  	;f
i 1 	1.2500	0.2500	 0.5 	 8.02 	  	;d
i 1 	1.5000	0.2500	 0.5 	 8.04 	  	;e
i 1 	1.7500	0.2500	 0.5 	 8.00 	  	;c
i 1 	2.0000	0.5000	 0.5 	 8.07 	  	;g
i 1 	2.5000	0.5000	 0.5 	 9.00 	  	;c9
i 1 	3.0000	0.5000	 0.5 	 8.11 	  	;b8
i 1 	3.5000	0.5000	 0.5 	 9.00 	  	;c9
;bottom
i 1 	2.2500	0.2500	 0.5 	 7.00 	  	;c
i 1 	2.5000	0.2500	 0.5 	 7.02 	  	;d
i 1 	2.7500	0.2500	 0.5 	 7.04 	  	;e
i 1 	3.0000	0.2500	 0.5 	 7.05 	  	;f
i 1 	3.2500	0.2500	 0.5 	 7.02 	  	;d
i 1 	3.5000	0.2500	 0.5 	 7.04 	  	;e
i 1 	3.7500	0.2500	 0.5 	 7.00 	  	;c
;Measure4 2    
;top
i 1 	4.0000	0.2500	 0.5 	 9.02 	  	;d9
i 1 	4.2500	0.2500	 0.5 	 8.07 	  	;g8
i 1 	4.5000	0.2500	 0.5 	 8.09 	  	;a
i 1 	4.7500	0.2500	 0.5 	 8.11 	  	;b
i 1 	5.0000	0.2500	 0.5 	 9.00 	  	;c9
i 1 	5.2500	0.2500	 0.5 	 8.09 	  	;a8
i 1 	5.5000	0.2500	 0.5 	 8.11 	  	;b
i 1 	5.7500	0.2500	 0.5 	 8.07 	  	;g
i 1 	6.0000	0.5000	 0.5 	 9.02 	  	;d9
i 1 	6.5000	0.5000	 0.5 	 9.07 	  	;g
i 1 	7.0000	0.5000	 0.5 	 9.05 	  	;f
i 1 	7.5000	0.5000	 0.5 	 9.07 	  	;g
;bottom
i 1 	4.0000	0.5000	 0.5 	 7.07 	  	;g7
i 1 	4.5000	0.5000	 0.5 	 6.07 	  	;g6
i 1 	6.2500	0.2500	 0.5 	 7.07 	  	;g
i 1 	6.5000	0.2500	 0.5 	 7.09 	  	;a
i 1 	6.7500	0.2500	 0.5 	 7.11 	  	;b
i 1 	7.0000	0.2500	 0.5 	 8.00 	  	;c8
i 1 	7.2500	0.2500	 0.5 	 7.09 	  	;a7
i 1 	7.5000	0.2500	 0.5 	 7.11 	  	;b
i 1 	7.7500	0.2500	 0.5 	 7.07 	  	;g
;Measure4 3    
   
e
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
