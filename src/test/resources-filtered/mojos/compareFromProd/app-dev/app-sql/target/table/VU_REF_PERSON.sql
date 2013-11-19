
/* ============================================================ */
/*   Generation Automatique : VU_REF_PERSON                */
/* ============================================================ */

if exists (select 1 from sysobjects where type = 'V' and name = 'VU_REF_PERSON')
begin
   drop view VU_REF_PERSON
   print 'View VU_REF_PERSON dropped'
end
go

/* ============================================================ */
create view VU_REF_PERSON
 as
    select 1 as ID from REF_PERSON
go

/* ============================================================ */
if exists (select 1 from sysobjects where type = 'V' and name = 'VU_REF_PERSON')
   print 'View VU_REF_PERSON created'
go

