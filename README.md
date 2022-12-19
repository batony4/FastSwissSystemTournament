# Что надо сказать перед турниром

## Выбор количества столов
Берём столов от 30% до 40% от числа участников – меньше столов, если хотим более качественный выбор игр с ровными по уровню соперниками,
больше столов если хотим меньше пауз. Столы гарантированно всегда будут заполнены.
Проблема с выбором равных по уровню соперников может встать только в конце турнира и только в случае, если число матчей,
которые должен сыграть каждый игрок, близко к общему числу игроков (то есть каждый должен сыграть почти со всеми).

## Памятка организатору турнира (перед началом)
1. Необходимо внести настройки ("Столов", "Матчей", а также сформировать список игроков), убедиться что список сыгранных игр пуст.
2. Если число матчей, которые должен сыграть каждый человек, сильно меньше общего числа участников турнира, то первый круг лучше
составить вручную. По посеву игроков по рейтингу. Например, на 12 человек: 1 с 7, 2 с 8, ..., 6 с 12.
Если же каждый в любом случае должен сыграть почти с каждым, то расставлять игроков по силе в первом круге не нужно,
пусть машина всё сделает автоматически.
3. Если организатор тоже играет, то обязательно рассказать участникам, как пользоваться системой самостоятельно.

### О гибкости программы
Система очень гибкая. Вот, например, что она позволяет:
1. Опоздавший человек может войти в турнир в любой момент. Ему с самого начала будут подбираться подходящие по уровню соперники,
и он будет играть чаще, пока не догонит остальных по количеству игр.
2. Можно в любой момент сменить количество доступных для игры столов: программа сразу подстроится и будет заполнять столы полностью.
3. Можно в любой момент поменять количество матчей, которые должен сыграть каждый игрок. Система корректно подстроится.
Очевидно, система ничего не сможет сделать в случае, если кто-то уже сейчас сыграл больше матчей, чем нужно всего.
4. Организатор турнира в любой момент может вместо предложенных программой пар составить пару по своему желанию. Система это корректно
обработает и учтёт это в своих дальнейших действиях.
5. Можно вносить любые исправления в прошлом: например, менять результат матча, убрать матч из списка сыгранных игр, добавить матч в
список сыгранных игр, исправить состав сопернико
6. Важно: абсолютно запрещается сыграть друг с другом больше одного раза. Программа никогда не поставит какую-то пару второй раз. И при
исправлении истории, также нельзя указывать, что какие-то игроки сыграли друг с другом больше одного раза.
7. Также нельзя исключить из турнира игрока, который сейчас сыграл хотя бы один раз. Удалить его из турнира можно только вместе
со всей историей сыгранных им игр (эти результаты также удалятся и у его соперников).

Программа всегда следит за тем, чтобы сетка корректно складывалась при том, что каждый должен сыграть строго заданное количество игр.
Например, никогда не будет ситуации, что двум игрокам осталось доиграть по одной игре, но между собой они уже играли. Программа разрулит
эту ситуацию заблаговременно, выставив все пары так, чтобы в такую ситуацию не попасть.
Но это может оказаться невозможным в случае, если организатор турнира не следует рекомендациям программы, а ставит пары сам. Либо если
организатором внесено исправление в историю сыгранных игр, которое привело к тому, что спланировать оставшиеся игры корректно невозможно.


## Памятка участникам, как пользоваться программой самостоятельно во время турнира

### Если ты доиграл свой матч
1. Внести в файл tournament.txt свой результат. Просто через пробел результат первого и второго игрока.
2. Запустить программу (нажать на зелёный треугольник наверху экрана).
3. Посмотреть, кого программа следующими ставит к столу. Обязательно (!!!) им об этом сообщить.
4. При желании, можно посмотреть внизу экрана обновлённую таблицу.

### Как читать таблицу
1. Таблица отсортирована по текущему месту игрока в турнире.
2. Затем указано количество сыгранных игр. Если игрок играет очередную игру сейчас — после числа выводится звёздочка.
3. Текущее место определяется количеством побед. При равенстве — балансом выигранных сетов.
При равенстве — [коэффициентом Бергера](https://ru.m.wikipedia.org/wiki/Коэффициент_Бергера) по этим же двум величинам, то есть,
количеством побед и балансом сетов у всех, с кем вы сыграли (в итоге те, кто сыграл с более сильными соперниками, будут в таблице выше).
4. Так как в середине турнира у разных игроков может быть разное количество сыгранных на данный момент игр, в скобках к каждому показателю
указывается то же значение, делённое на количество сыгранных матчей. И в середине турнира сортировка таблицы производится именно по этому
показателю.
5. В конце в таблице выводится матрица сыгранных игр.
