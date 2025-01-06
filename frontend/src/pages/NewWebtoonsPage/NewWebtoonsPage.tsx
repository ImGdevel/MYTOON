import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import WebtoonService from '@services/webtoonService';
import WebtoonGrid from '@components/WebtoonGrid';
import Pagination from '@components/Pagination';
import styles from './NewWebtoonsPage.module.css';
import { Webtoon } from '@models/webtoon';

export interface NewWebtoonsPageState {
  webtoons: Webtoon[];
  totalPages: number;
  currentPage: number;
  isLoading: boolean;
  error: string | null;
}


const NewWebtoonsPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [state, setState] = useState<NewWebtoonsPageState>({
    webtoons: [],
    currentPage: 1,
    totalPages: 1,
    isLoading: true,
    error: null
  });

  useEffect(() => {
    const fetchNewWebtoons = async () => {
      try {
        const page = Number(searchParams.get('page')) || 1;
        const response = await WebtoonService.getRecentWebtoons(page);

        setState((prev) => ({ 
          ...prev,
          webtoons: response.data || [],
          currentPage: page,
          totalPages: Math.ceil((response.total || 0) / 20),
          isLoading: false,
          error: null
        }));
      } catch (error) {
        setState(prev => ({
          ...prev,
          isLoading: false,
          error: '새로운 웹툰을 불러오는데 실패했습니다.'
        }));
      }
    };

    fetchNewWebtoons();
  }, [searchParams]);

  const handlePageChange = (page: number) => {
    setSearchParams({ page: page.toString() });
  };

  if (state.isLoading) return <div>로딩중...</div>;
  if (state.error) return <div>{state.error}</div>;

  return (
    <div className={styles.newWebtoonsPage}>
      <h1>신작 웹툰</h1>
      <WebtoonGrid webtoons={state.webtoons} />
      <Pagination 
        currentPage={state.currentPage}
        totalPages={state.totalPages}
        onPageChange={handlePageChange}
      />
    </div>
  );
};

export default NewWebtoonsPage; 