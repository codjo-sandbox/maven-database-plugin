
/* ============================================================ */
/*   Generation Automatique : VU_REF_GROUP                */
/* ============================================================ */

if exists (select 1 from sysobjects where type = 'V' and name = 'VU_REF_GROUP')
begin
   drop view VU_REF_GROUP
   print 'View VU_REF_GROUP dropped'
end
go

/* ============================================================ */
create view VU_REF_GROUP
 as
    select 1 as ID from REF_GROUP
go

/* ============================================================ */
if exists (select 1 from sysobjects where type = 'V' and name = 'VU_REF_GROUP')
   print 'View VU_REF_GROUP created'
go

