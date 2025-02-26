Squid Game 2
====
A comprehensive game manager and game set comprised of five games(달고나 게임, 둥글게 둥글게 게임, 무궁화 게임, 기차 게임, 러시안 룰렛 게임), which is (mainly) live-streamed at [우왁굳](https://ch.sooplive.co.kr/ecvhao).  
Our team members allowed me to make this project public ❤️

Before getting started
----

All of media resources (sounds, images, models .etc) are not public. You should create your own! 

Getting Started
-----

This project is composed of components. The root project has a core system that retains game context and controls overall lifecycles of games currently available.  
Subprojects, which inherit root project, implement each game originated from Squid Game 2 plus our custom game(trolley game).  
It has IoC structure so that each game does not have to take care of lifecycle. One who wants to implement a new game must inherit the core system and register to them.  
It might seem to be a little bit clumsy, but it does provide programmers useful tools to focus more on details.  
For more information, please check out some games already implemented. Although there are no comments, you'd have comprehensive insights over it.  

- `dalgona` - `달고나 게임`
- `mgr` - `둥글게 둥글게 게임`
- `rlgl` - `무궁화 게임`
- `roulette` - `러시안 룰렛 게임`
- `trolley` - `기차 놀이 게임`

And there are also psuedo implementations that handle extra operations for game context:  

- `intermediary`: A game that handles all events fired in terms between games. Players can be killed, kicked by administrators at this period.

Note
----

This project and its codes are licensed under GPL-3.0
