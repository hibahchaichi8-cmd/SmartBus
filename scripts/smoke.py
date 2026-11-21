"""Integration smoke test. Creates QA records in the RUNNING application's database.
Use an isolated test environment. Requires API_BASE, ADMIN_EMAIL and ADMIN_PASSWORD.
No passwords or tokens are printed. Python 3 standard library only.
"""
import os,json,uuid,datetime,urllib.request,urllib.error,urllib.parse
BASE=os.environ.get('API_BASE','http://localhost:3000/api/v1').rstrip('/')
count=0

def call(method,path,body=None,token=None,status=200):
 global count
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 request=urllib.request.Request(BASE+path,data=None if body is None else json.dumps(body).encode(),headers=headers,method=method)
 try:
  with urllib.request.urlopen(request,timeout=15) as response:actual=response.status;raw=response.read()
 except urllib.error.HTTPError as error:actual=error.code;raw=error.read()
 result=json.loads(raw) if raw else None
 if actual!=status:raise AssertionError(f'{method} {path}: expected {status}, got {actual}: {result}')
 if isinstance(result,(dict,list)) and 'passwordHash' in json.dumps(result):raise AssertionError('Password hash leaked')
 count+=1
 return result

def run():
 tag=uuid.uuid4().hex[:8];password='QaPassword12345';email='qa'+tag+'@example.test'
 call('POST','/buses',{},status=401)
 session=call('POST','/auth/register',{'fullName':'QA Citizen','email':email,'password':password,'role':'ADMIN'},status=201);citizen=session['accessToken'];assert session['user']['role']=='CITIZEN'
 call('POST','/auth/register',{'fullName':'Duplicate','email':email,'password':password},status=409)
 call('POST','/auth/login',{'email':email,'password':'wrong'},status=401)
 call('POST','/buses',{'registrationNumber':'X','capacity':50},citizen,status=403)
 call('GET','/auth/me',token=citizen)
 admin=call('POST','/auth/login',{'email':os.environ['ADMIN_EMAIL'],'password':os.environ['ADMIN_PASSWORD']})['accessToken']
 altered=admin[:-10]+('A' if admin[-10]!='A' else 'B')+admin[-9:]
 call('GET','/drivers',token=altered,status=401)
 bus=call('POST','/buses',{'registrationNumber':'QA-'+tag,'capacity':40},admin,status=201)
 call('PUT','/buses/'+bus['id'],{'registrationNumber':'QA-'+tag,'capacity':45},admin)
 call('POST','/buses',{'registrationNumber':'QA-'+tag,'capacity':45},admin,status=409)
 call('POST','/buses',{'registrationNumber':'BAD','capacity':0},admin,status=400)
 a=call('POST','/stations',{'name':'QA Origin '+tag,'latitude':36.8,'longitude':10.1},admin,status=201)
 b=call('POST','/stations',{'name':'QA Destination '+tag,'latitude':36.82,'longitude':10.15},admin,status=201)
 route=call('POST','/routes',{'code':tag,'name':'QA Route','stops':[{'stationId':a['id'],'offsetMinutes':0},{'stationId':b['id'],'offsetMinutes':25}]},admin,status=201)
 fare=call('POST','/fares',{'routeId':route['id'],'amountMillimes':1500},admin,status=201)
 call('PUT','/schedules/'+route['id'],{'firstDeparture':'06:00','lastDeparture':'22:00','intervalMinutes':20},admin)
 call('PUT','/schedules/'+route['id'],{'firstDeparture':'22:00','lastDeparture':'06:00','intervalMinutes':20},admin,status=400)
 query=urllib.parse.urlencode({'originId':a['id'],'destinationId':b['id'],'departure':'2026-11-20T06:01'})
 journeys=call('GET','/journeys?'+query);assert journeys[0]['amountMillimes']==1500 and journeys[0]['durationMinutes']==25;assert journeys[0]['departure'].endswith('05:20:00Z')
 call('DELETE','/stations/'+a['id'],token=admin,status=409)
 driver=call('POST','/drivers',{'fullName':'QA Driver','email':'driver'+tag+'@example.test','password':password},admin,status=201)
 other=call('POST','/drivers',{'fullName':'Other Driver','email':'other'+tag+'@example.test','password':password},admin,status=201)
 driverToken=call('POST','/auth/login',{'email':driver['email'],'password':password})['accessToken'];otherToken=call('POST','/auth/login',{'email':other['email'],'password':password})['accessToken']
 departure=(datetime.datetime.now(datetime.timezone.utc)+datetime.timedelta(hours=1)).isoformat()
 trip=call('POST','/trips',{'routeId':route['id'],'busId':bus['id'],'driverId':driver['id'],'plannedDeparture':departure},admin,status=201)
 call('POST','/trips',{'routeId':route['id'],'busId':bus['id'],'driverId':other['id'],'plannedDeparture':departure},admin,status=409)
 call('GET','/trips/mine',token=driverToken)
 call('POST','/trips/'+trip['id']+'/start',{},otherToken,status=403)
 call('PUT','/tracking/'+trip['id']+'/occupancy',{'passengerCount':4},driverToken,status=409)
 call('POST','/trips/'+trip['id']+'/start',{},driverToken)
 call('POST','/trips/'+trip['id']+'/finish',{},driverToken,status=409)
 call('PUT','/tracking/'+trip['id']+'/occupancy',{'passengerCount':46},driverToken,status=400)
 call('PUT','/tracking/'+trip['id']+'/occupancy',{'passengerCount':12},otherToken,status=403)
 call('PUT','/tracking/'+trip['id']+'/occupancy',{'passengerCount':12},driverToken)
 now=datetime.datetime.now(datetime.timezone.utc).isoformat();position={'latitude':36.8,'longitude':10.1,'observedAt':now,'source':'SIMULATED'}
 call('PUT','/tracking/'+trip['id']+'/position',position,driverToken)
 call('PUT','/tracking/'+trip['id']+'/position',position,driverToken,status=409)
 reports=call('GET','/tracking');report=next(r for r in reports if r['tripId']==trip['id']);assert report['seatsAvailable']==33 and report['stale']==False and 'driverId' not in report
 call('POST','/trips/'+trip['id']+'/checkpoints',{'stopIndex':1},driverToken,status=409)
 for index in range(2):call('POST','/trips/'+trip['id']+'/checkpoints',{'stopIndex':index},driverToken)
 call('POST','/trips/'+trip['id']+'/finish',{},driverToken)
 assert not any(r['tripId']==trip['id'] for r in call('GET','/tracking'))
 call('PUT','/tracking/'+trip['id']+'/occupancy',{'passengerCount':1},driverToken,status=409)
 call('DELETE','/schedules/'+route['id'],token=admin,status=204);call('DELETE','/fares/'+fare['id'],token=admin,status=204);call('DELETE','/routes/'+route['id'],token=admin,status=204)
 for station in [a,b]:call('DELETE','/stations/'+station['id'],token=admin,status=204)
 call('DELETE','/buses/'+bus['id'],token=admin,status=204)
 print(f'PASS: {count} HTTP checks plus fare, ETA, privacy and state assertions.')

if __name__=='__main__':run()
