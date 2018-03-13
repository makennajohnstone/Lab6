select * from lab6_rooms
  join lab6_reservations on RoomCode = Room
  where RoomCode not in
    (select RoomCode from lab6_rooms
      join lab6_reservations on RoomCode = Room
      where (Checkout >= STR_TO_DATE('2018-10-26', '%Y-%m-%d') and CheckIn < STR_TO_DATE('2018-10-28', '%Y-%m-%d'))
    )
;

insert into mejohnst.lab6_reservations (CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids)
  values(99864, 'HBB', STR_TO_DATE('2018-03-19', '%Y-%m-%d'), STR_TO_DATE('2018-03-18', '%Y-%m-%d'), 410.02, 'Johnstone', 'Makenna', 2, 0);

  SELECT lab6_rooms.RoomCode, (SELECT
     CASE
     WHEN not exists (
       
       SELECT second.CheckIn
       FROM lab6_reservations second
       WHERE second.Room = first.Room AND second.Checkout > CURDATE() AND second.CheckIn <= CURDATE()
     ) THEN CURDATE()
      

     ELSE min(Checkout)
     END

     FROM lab6_reservations first
     WHERE first.Room = lab6_rooms.RoomCode AND first.CheckIn >= CURDATE() AND not exists (
       SELECT *  -- with EXISTS, projection doesn't matter, could be any column or all
       FROM lab6_reservations third
       WHERE third.Room = first.Room and third.CheckIn = first.CheckOut
     )

    ) as FirstAvailable
  from lab6_rooms;



  SELECT lab6_rooms.*, (SELECT
   CASE
   WHEN not exists (
     
     SELECT second.CheckIn
     FROM lab6_reservations second
     WHERE second.Room = first.Room AND second.Checkout > CURDATE() AND second.CheckIn <= CURDATE()
   ) THEN CURDATE()
    

   ELSE min(first.CheckOut)
   END

   FROM lab6_reservations first
   WHERE first.Room = lab6_rooms.RoomCode AND first.CheckIn >= CURDATE() AND not exists (
     SELECT *  -- with EXISTS, projection doesn't matter, could be any column or all
     FROM lab6_reservations third
     WHERE third.Room = first.Room and third.CheckIn = first.CheckOut
   )

  ) as FirstAvailable, A1.Length, A1.RecentCheckOut, A2.PopularityScore
FROM lab6_rooms

JOIN

(SELECT lab6_reservations.Room as Room, DATEDIFF(MAX(lab6_reservations.CheckOut), MAX(lab6_reservations.CheckIn)) as Length, MAX(lab6_reservations.CheckOut) as RecentCheckOut
FROM lab6_reservations
WHERE lab6_reservations.CheckOut <= CURDATE()
GROUP BY lab6_reservations.Room) A1 ON A1.Room = lab6_rooms.RoomCode

JOIN

(SELECT lab6_rooms.RoomCode as Room,  ROUND((SUM(DATEDIFF(LEAST(lab6_reservations.CheckOut, CURDATE()), GREATEST(lab6_reservations.CheckIn, DATE_ADD(lab6_reservations.CheckIn, INTERVAL -180 DAY))))/180), 2) as PopularityScore
FROM lab6_reservations
JOIN lab6_rooms ON lab6_rooms.RoomCode = lab6_reservations.Room
WHERE (lab6_reservations.CheckIn <= CURDATE()) AND (lab6_reservations.CheckOut >= DATE_ADD(lab6_reservations.CheckIn, INTERVAL -180 DAY))
GROUP BY lab6_reservations.Room) A2 ON A2.Room = lab6_rooms.RoomCode
ORDER BY A2.PopularityScore DESC;
