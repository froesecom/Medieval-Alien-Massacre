
; rooms.clj
; Defines data structures for describing rooms,
; maps, objects and inventories.

(in-ns 'mam.gameplay)
(declare set-current-room! in-inventory? mam-pr can-afford?
         hit-milestone? add-milestone! credits)

(ns mam.rooms
  (:use mam.gameplay))


(def keycards {:green 4 :red 5 :silver 6})

(defn check-key [key-type room]
  "Checks if the player has the given type of security card. If they do, set the current
   room to 'room'. Otherwise, let them know"
  (if (in-inventory? (keycards key-type))
    (let [key-name (apply str (rest (str key-type)))]
      (set-current-room! room)
      (mam-pr (str " * Door unlocked with " key-name " keycard. *")))
    (mam-pr "You don't have security clearance for this door!")))

; A vector of rooms. Each index contains both a large description (first visit) and a brief
; description (all subsequent visits).
(def rooms
  (vector
    '("You are in a small, silver-walled room with no windows. There is a door to the north labelled 'Repairs deck' and another door to the east."
      "Empty room with a bed. Doors to north and east.")
    '("You are in another small, featureless room. There is nothing of interest here except doors to the north and west."
      "Small, featureless room. Doors to north and west.")
    '("You enter a control room with a few blank screens. There are doors to the east and west."
      "Control room with doors to east and west")
    '("There is a long row of broken flying machines here. A large sign reads 'Repairs deck: West end'. 'Where the fuck am I?' you think to yourself. The passage leads east. There is a door to the south."
      "West-end of the repairs deck. Passage leads east. Door to the south.")
    '("You walk into a hallway with doors to your west and south. The door to the west has a sign reading 'Repairs deck'. The hallway is leading north."
      "Hallway. Doors to the west and south. Passage leads north.")
    '("You continue along the passage and pass more broken machines. Passage leads east or west."
      "Repairs deck, center. Passage leads west/east.")
    '("You are at the end of the hallway. There is a large, sliding door to the north."
      "End of hallway. Large door to north.")
    '("There are a bunch broken machines lying around on the repairs deck. There is a door to the east or a passage south."
      "Repairs deck. Door to the east and passage south.")
    '("You are in a large room with space age decor. It seems to be the central living quarters. The walls are lined with pictures of the late comedian, Bill Hicks. There are walkways to the west and northwest and a door to the south."
      "Central living quarters, walkways to west and northwest, door to south.")
     '("You can see some more framed pictures of Bill Hicks here. As you walk past them, Bills eyes seem to follow you. The passage goes west or east."
       "Passage with more creepy Bill Hicks pictures. Passage leads east/west.")
     '("You are at the west-end of the room. Here you can see sealed entrance and a sign saying 'Exit pod'."
       "West-end of large room with exit pod.")
     '("You are at the front of the large room. There is a huge glass-like window here and you can see now that you are, infact, travelling through space! There are passages going back southeast and southwest."
       "Front of large room with huge glass-like window. Passages southeast/southwest.")))

; Map to specify which rooms the player will enter on the given movement.
; A function indicates that something special needs to be done (check conditions, etc).
(def world-map
  (vector
;    north         east         south        west         northeast    southeast    southwest    northwest
    [3             2            nil          nil          nil          nil          nil          nil]   ;0
    [4             nil          nil          2            nil          nil          nil          nil]   ;1
    [nil           1            nil          0            nil          nil          nil          nil]   ;2
    [nil           5            0            nil          nil          nil          nil          nil]   ;3
    [6             nil          1            7            nil          nil          nil          nil]   ;4
    [nil           7            nil          3            nil          nil          nil          nil]   ;5
    [#(check-key
       :green 8)   nil          4            nil          nil          nil          nil          nil]   ;6
    [nil           4            nil          5            nil          nil          nil          nil]   ;7
    [nil           nil          6            9            nil          nil          nil          11]    ;8
    [nil           8            nil          10           nil          nil          nil          nil]   ;9
    [nil           9            nil          nil          11           nil          nil          nil]   ;10
    [nil           nil          nil          nil          nil          8            10           nil])) ;11

(def directions {'north 0 'east 1 'south 2 'west 3 'northeast 4
                 'southeast 5 'southwest 6 'northwest 7})

; Specifies the verbs that users can identify an object with (a gun might
; be "gun", "weapon", etc). Permanent objects (people, beds, etc) may be identified
; by the same word depending on the room (in room 1, "bed" means object 2, but in
; room 42, it means object 8). Each index corresponds to the same index in room-objects.
(def object-identifiers
    {'candy 0 'bar 0 'bed {0 1} 'lever {2 2} 'mag 3 'magazine 3 'porno 3 'boy {7 7}
     'teenager {7 7} 'keycard {7 4} 'key {7 4} 'man {10 8, 11 9} 'robot {11 10}})

; A vector containing the objects that each room contains when the game starts. Each index
; corresponds to the room as defined in 'rooms'.
(def room-objects
  (ref (vector
         [0 1]
         []
         [2]
         []
         []
         []
         []
         [7]
         []
         []
         [8]
         [9 10])))

; Some living objects have special speech considerations, such as checking conditions.
; Here I keep a bunch of functions that are assigned to the relevent objects in object-details.
(def speech-fn-for
  {:pod-manager
     #(cond
        (not (can-afford? 3))
          (mam-pr "The man says 'Hey, I can get your sorry ass off this ship, but it will cost you 3 credits. Come back when you can afford it, matey'.")
        (not (hit-milestone? :speak-to-captain))
          (mam-pr "The man says 'Hey matey, I can get your sorry ass off here, but I suggest you speak to the captain over there to our northeast first'.")
        :else
          (mam-pr "The man says 'Oky doke, matey, lets get your punk ass outta' here. I hope Syndal City on Jupiter 4 is alright'.")),
   :repairs-captain
     #(if (hit-milestone? :speak-to-captain)
        (mam-pr "The captain says 'That is all the information I have. Now, fuck off before I get mad.'.")
        (do
          (mam-pr "The man says 'Ahh, you're up! I am Bob Benson, the captain of this grand model T102 repairs vessel. We found you floating out there on the oxygenated stretch of galactic highway 7. Anyway, you look a tad confused, so let me refresh your memory:")
          (mam-pr "It is the year 2843, you're currently travelling on a highway between two of the moons of Jupiter.")
          (mam-pr "\n** At this point you explain that you are infact from the year 2010 and the last thing you remember is driking coffee at home and writing some Lisp code **\n")
          (mam-pr "The captain says 'Oh, yes, it makes sense now. A true Lisp hacker and drinker of the finest bean can transcend both space and time. We've seen your type before. You should head over to see the Pod Manager to our southwest in order to get yourself off this ship'")
          (add-milestone! :speak-to-captain)))})

; Giving a certain x to a certain y will cause special things to happen.
(def give-fn-for
  {:porno-to-boy
     #(dosync
        (mam-pr "The teenagers eyes explode!! He quickly accepts the porno mag and runs away. He throws a green keycard in your general direction as he leaves the room.")
        (take-object-from-room! @current-room 7)
        (drop-object-in-room! @current-room 4))})

; Functions to execute when player eats particular objects.
(def eat-fn-for
  {:eats-candy
     #(dosync
        (mam-pr "You feel like you just ate crusty skin off Donald Trump's forehead. Although inside the wrapper there was an 'instant win' of 5 credits!")
        (alter credits + 5))})

(defn make-dets [details]
  "A helper function to merge in some sane defaults for object details"
  (let [defaults {:inv nil, :weight nil, :edible false, :permanent false, :living false
                  :events {}}] ;;:speech nil, :giveables {}, :putables {}}]
    (merge defaults details)))

; The details of all objects. Each object is assigned a number in object-identifiers, which
; corresponds to it's index here. Permanent object cannot be taken and thus don't require
; weights or inventory descriptions. Humans/Aliens can talk, the :speech symbol should contain
; their response (this can be a function, for checking conditions, etc).
(def object-details
  (vector
    (make-dets {:game "There is a tasty-looking candy bar here"
                :inv "A candy bar"
                :inspect "It's called 'Space hack bar' and there is a competition running according to the wrapper"
                :weight 1
                :events {:eat (eat-fn-for :eats-candy)}}),
    (make-dets {:game "There is a small bed here"
                :inspect "It's black and sorta' small looking. Perhaps for a child?"
                :permanent true}),
    (make-dets {:game "There is a large metal lever here"
                :inspect "There is no label, but it seems to have some wear from usage"
                :permanent true}),
    (make-dets {:game "There is a porno mag here"
                :inv "A porno mag"
                :inspect "The title is 'Humaniod Whores, vol #995, June 2351'"
                :weight 2}),
    (make-dets {:game "There is a green keycard here"
                :inv "Green keycard"
                :inspect "It says 'All access: Green'"
                :weight 1}),
    (make-dets {:game "There is a red keycard here"
                :inv "Red keycard"
                :inspect "It says 'All access: Red'"
                :weight 1}),
    (make-dets {:game "There is a silver keycard here"
                :inv "Silver keycard"
                :inspect "It says 'All access: Silver'"
                :weight 1}),
    (make-dets {:game "There is a teenage alien boy here!"
                :inspect "He is excitedly looking for something..."
                :permanent true
                :events {:give {3 (give-fn-for :porno-to-boy)},
                         :speak "He mentions that he's looking for 'some ill pronz with Sasha Grey'. You nod, knowingly"}
                :living true}),
    (make-dets {:game "There is an Alien man here"
                :inspect "He is wearing a nice uniform and has a tag that says 'Pod manager'"
                :permanent true
                :events {:speak (speech-fn-for :pod-manager)}
                :living true}),
    (make-dets {:game "There is an important-looking Alien man here"
                :inspect "He is wearing a stupid blonde wig, but looks friendly"
                :permanent true
                :events {:speak (speech-fn-for :repairs-captain)}
                :living true}),
    (make-dets {:game "There is a small robot here"
                :inspect "He looks a bit like R2D2, but without the lights. There seems to be a vac-u-lock Dildo sticking out of his forehead."
                :permanent true
                :events {:speak "The robot says 'Hello, I am Nexus model 19, series 4. It seems to me that you are not from around here. Perhaps you are lost? Regardless, I have but one thing to tell you, and that, of course, is the meaning to life. The answer is, simply stated in Human tongue, the persuit of excellence in Skateboarding.'"}
                :living true})))

(def *total-weight* 12)
